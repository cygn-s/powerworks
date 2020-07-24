package screen.mouse.tool

import io.*
import level.Level
import level.LevelManager
import level.LevelObject
import main.Game
import screen.mouse.Mouse

// activate
// deactivate
// make active if activator keys are pressed
//

abstract class Tool2(vararg val activators: Control) : ControlPressHandler {
    constructor(activators: Control.Group) : this(*activators.controls)

    var currentlyHeldActivators = mutableListOf<Control>()
    var currentlyActive = false

    abstract fun onInteract()

    open fun update() {}

    open fun renderBelow() {}

    open fun renderAbove() {}

    override fun handleControlPress(p: ControlPress) {
        if (p.control in activators) {
            if (p.pressType == PressType.PRESSED) {
                currentlyHeldActivators.add(p.control)
            } else if (p.pressType == PressType.RELEASED) {
                currentlyHeldActivators.remove(p.control)
            }
            currentlyActive = currentlyHeldActivators.isNotEmpty()
        }
    }
}

/**
 * A tool is an object that is used by the player to interact with the level, usually through the mouse
 * @param use the controls to send to this tool when they are pressed/repeated/released. These should be what will 'use' the tool
 */
abstract class Tool(vararg val use: Control) : ControlPressHandler {

    constructor(use: Control.Group) : this(*use.controls)

    /**
     * Whether or not this tool is currently able to be used
     */
    var currentlyActive = false
        set(value) {
            if (value && !field) {
                ALL_ACTIVE.add(this)
                field = value
            } else if (!value && field) {
                ALL_ACTIVE.remove(this)
                field = value
            }
        }

    init {
        ALL.add(this)
        if (!Game.IS_SERVER) {
            InputManager.registerControlPressHandler(this, ControlPressHandlerType.LEVEL_ANY_UNDER_MOUSE, *use)
        }
    }

    override fun handleControlPress(p: ControlPress) {
        if (currentlyActive) {
            onUse(p.control, p.pressType, LevelManager.mouseLevelXPixel, LevelManager.mouseLevelYPixel, Mouse.button, InputManager.inputsBeingPressed.contains("SHIFT"), InputManager.inputsBeingPressed.contains("CONTROL"), InputManager.inputsBeingPressed.contains("ALT"))
        }
    }

    /**
     * This is called when a control from [Tool.use] is pressed/repeated/released, and [currentlyActive] is true. Tools are
     * unable to be used if the mouse isn't over a [Level], so [LevelManager.levelUnderMouse] can be assumed to be non-null
     */
    abstract fun onUse(control: Control, type: PressType, mouseLevelXPixel: Int, mouseLevelYPixel: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean)

    /**
     * Updates the [currentlyActive] variable to the correct value (true if this tool should be able to be used, false otherwise)
     * For example, if the [Tool.Interactor] tool were over a level object with [LevelObject.isInteractable] set to true, this should set currentlyActive to true
     */
    abstract fun updateCurrentlyActive()

    /**
     * Whatever is drawn here is drawn above all level objects.
     * This is only called if [currentlyActive] is true
     */
    open fun renderAbove() {}

    /**
     * Whatever is drawn here is drawn below all level objects.
     * This is only called if [currentlyActive] is true
     */
    open fun renderBelow() {}

    /**
     * An updater called once every game update, but only if [currentlyActive] is true
     */
    open fun update() {}

    companion object {

        val ALL = mutableListOf<Tool>()

        val ALL_ACTIVE = mutableListOf<Tool>()

        init {
            BlockRemover
            BlockPlacer
            Interactor
            Teleporter
            Selector
            ResourceNodeEditor
            EntityPlacer
            EntityController
            BlockPicker
            // todo priority: if onUse returns true and this is the highest priority, none of the other controls will go through
        }

        fun renderBelow() {
            ALL.forEach { it.renderBelow() }
        }

        fun renderAbove() {
            ALL.forEach { it.renderAbove() }
        }

        fun update() {
            ALL.forEach { it.updateCurrentlyActive() }
            ALL_ACTIVE.forEach { it.update() }
        }
    }
}