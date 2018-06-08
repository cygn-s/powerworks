package screen

import io.*
import data.ConcurrentlyModifiableMutableList
import data.ConcurrentlyModifiableWeakMutableList
import data.WeakMutableList
import graphics.Renderer
import main.Game
import misc.GeometryHelper
import screen.elements.GUIWindow
import screen.elements.RootGUIElement
import java.awt.Rectangle

object ScreenManager : ControlPressHandler {

    private val _windowGroups = mutableListOf<WindowGroup>()
    val windowGroups = object : MutableList<WindowGroup> by _windowGroups {
        override fun add(element: WindowGroup): Boolean {
            val result = _windowGroups.add(element)
            _backwardsWindowGroups.add(element)
            sortByDescending { it.layer }
            _backwardsWindowGroups.sortBy { it.layer }
            return result
        }

        override fun remove(element: WindowGroup): Boolean {
            val result = _windowGroups.remove(element)
            _backwardsWindowGroups.remove(element)
            return result
        }
    }

    private val _backwardsWindowGroups = mutableListOf<WindowGroup>()
    val windows = ConcurrentlyModifiableWeakMutableList<GUIWindow>()
    internal val openWindows = WeakMutableList<GUIWindow>()

    /**
     * The last GUIElement interacted with. Keep in mind this doesn't look at elements with the transparentToInteraction flag set to true
     */
    var selectedElement: RootGUIElement? = null

    /**
     * The last GUIWindow interacted with. Keep in mind this doesn't look at elements with the transparentToInteraction flag set to true
     */
    var selectedWindow: GUIWindow? = null

    object Groups {
        val BACKGROUND = WindowGroup(0, "Background")
        val VIEW = WindowGroup(1, "GUIViews")
        val INVENTORY = WindowGroup(2, "Inventories")
        val PLAYER_UTIL = WindowGroup(3, "Player utilities")
        val HOTBAR = WindowGroup(4, "Hotbar")
        val MOUSE = WindowGroup(9999, "Mouse")
    }

    init {
        InputManager.registerControlPressHandler(this, ControlPressHandlerType.GLOBAL, Control.INTERACT, Control.SHIFT_INTERACT, Control.ALT_INTERACT, Control.CONTROL_INTERACT, Control.SCROLL_UP, Control.SCROLL_DOWN, Control.SECONDARY_INTERACT)
    }

    fun render() {
        _backwardsWindowGroups.forEach {
            it.windows.forEach {
                it.openChildren.forEach {
                    if (it.autoRender)
                        it.render()
                }
            }
        }
    }

    fun update() {
        updateMouseOn()
        forEachElement(func = { it.update() })
        openWindows.forEach { it.update() }
    }

    fun updateMouseOn() {
        forEachElement({ it.mouseOn = isMouseOn(it) }, { it.open })
    }

    fun isMouseOn(e: RootGUIElement): Boolean {
        return intersectsElement(Mouse.xPixel, Mouse.yPixel, e)
    }

    fun intersectsElement(xPixel: Int, yPixel: Int, e: RootGUIElement): Boolean {
        return GeometryHelper.contains(e.xPixel, e.yPixel, e.widthPixels, e.heightPixels, xPixel, yPixel, 0, 0)
    }

    fun intersectsElement(xPixel: Int, yPixel: Int, e: GUIWindow): Boolean {
        return GeometryHelper.contains(e.xPixel, e.yPixel, e.widthPixels, e.heightPixels, xPixel, yPixel, 0, 0)
    }

    /** @return the highest window, layer-wise, that intersects the given x and y coordinates and matches the predicate */
    fun getHighestWindow(xPixel: Int, yPixel: Int, predicate: (GUIWindow) -> Boolean = { true }): GUIWindow? {
        var g: GUIWindow? = null
        windowGroups.forEach {
            if (g == null) {
                g = it.windows.lastOrNull { it.open && intersectsElement(xPixel, yPixel, it) && predicate(it) }
            }
        }
        return g
    }

    /** @return the highest element, layer-wise, that intersects the given x and y coordinates and matches the predicate. */
    fun getHighestElement(xPixel: Int, yPixel: Int, window: GUIWindow? = getHighestWindow(xPixel, yPixel), predicate: (RootGUIElement) -> Boolean = { true }): RootGUIElement? {
        return window?.openChildren?.stream()?.filter {
            intersectsElement(xPixel, yPixel, it) && predicate(it)
        }?.max { o1, o2 -> o1.layer.compareTo(o2.layer) }?.orElseGet { null }
    }

    fun screenSizeChange(oldWidth: Int, oldHeight: Int) {
        windows.forEach {
            if (it.adjustDimensions) {
                it.widthPixels = it.widthAlignment()
                it.heightPixels = it.heightAlignment()
            }
            it.xPixel = it.xAlignment()
            it.yPixel = it.yAlignment()
            it.onScreenSizeChange(oldWidth, oldHeight)
        }
        Renderer.defaultClip = Rectangle(Game.WIDTH * Game.SCALE, Game.HEIGHT * Game.SCALE)
    }

    fun updateSelected() {
        val x = Mouse.xPixel
        val y = Mouse.yPixel
        selectedWindow = getHighestWindow(x, y, { !it.transparentToInteraction })
        val window = selectedWindow
        if (window != null) {
            window.windowGroup.bringToTop(window)
            selectedElement = getHighestElement(x, y, window, { !it.transparentToInteraction })
        }
        updateControlHandlers()
    }

    fun updateControlHandlers() {
        InputManager.currentScreenHandlers.clear()
        val x = Mouse.xPixel
        val y = Mouse.yPixel
        if (selectedElement is ControlPressHandler) {
            InputManager.currentScreenHandlers.add(selectedElement as ControlPressHandler)
        }
        if (selectedWindow != null) {
            if (selectedWindow is ControlPressHandler) {
                InputManager.currentScreenHandlers.add(selectedWindow as ControlPressHandler)
            }
            if (selectedWindow!!.partOfLevel) {
                val h = getHighestWindow(x, y, { it is LevelViewWindow }) as ControlPressHandler?
                if (h != null)
                    InputManager.currentScreenHandlers.add(h)
            }
        }
    }

// TODO update screen control handlers by going through the selected elements. if one is part of the level, add the gui view under it to the screen handler

    private fun forEachElement(func: ((RootGUIElement) -> Unit), pred: ((RootGUIElement) -> Boolean)? = null) {
        windows.forEach { recursivelyCall(it.rootChild, func, pred) }
    }

    private fun recursivelyCall(e: RootGUIElement, l: (RootGUIElement) -> Unit, f: ((RootGUIElement) -> Boolean)? = null) {
        if (f == null || f(e)) {
            l(e)
            e.children.forEach { recursivelyCall(it, l, f) }
        }
    }

    override fun handleControlPress(p: ControlPress) {
        val x = Mouse.xPixel
        val y = Mouse.yPixel
        val t = p.pressType
        val b = Mouse.button
        val c = p.control
        if (Control.Group.INTERACTION.contains(c)) {
            // If it is repeating, we don't want it to change the selected element so we can move the mouse nice and fast
            // without worrying that it will click on something else
            if (t == PressType.PRESSED)
                updateSelected()
            if (Control.Group.SCROLL.contains(c))
                selectedElement?.onMouseScroll(if (c == Control.SCROLL_UP) 1 else -1)
            else {
                selectedElement?.onMouseActionOn(t, x, y, b, (c == Control.SHIFT_INTERACT), (c == Control.CONTROL_INTERACT), (c == Control.ALT_INTERACT))
                forEachElement({ it.onMouseActionOff(t, x, y, b, (c == Control.SHIFT_INTERACT), (c == Control.CONTROL_INTERACT), (c == Control.ALT_INTERACT)) }, { it != selectedElement && it.open })
            }
            if (t == PressType.RELEASED)
                updateSelected()
        }
    }

    private fun printDebug() {
        fun RootGUIElement.print(spaces: String = ""): String {
            var v = spaces + toString()
            for (g in children)
                v += "\n" + g.print(spaces + "   ")
            return v
        }
        for (group in windowGroups) {
            println(group.name + ":")
            for (window in group.windows) {
                if (window.open) {
                    println("   $window:")
                    println(window.rootChild.print("      "))
                }
            }
        }
    }
}