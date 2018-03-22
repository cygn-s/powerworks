package screen

import graphics.Image
import graphics.Renderer
import graphics.Texture
import graphics.Utils
import io.*
import item.Inventory
import item.ItemType
import level.CHUNK_PIXEL_EXP
import level.DroppedItem
import level.Level
import level.LevelObject
import level.tube.TubeBlock
import main.Game
import main.State
import resource.ResourceContainer
import resource.ResourceContainerChangeListener
import resource.ResourceType
import screen.elements.*

object Mouse : ControlPressHandler, ResourceContainerChangeListener {
    const val DROPPED_ITEM_PICK_UP_RANGE = 8

    const val ICON_SIZE = 8

    var button = 0
    var xPixel = 0

    var yPixel = 0
    var heldItemType: ItemType? = null

    private val levelTooltipTemplates = sortedMapOf<Int, (LevelObject) -> String?>()

    private val screenTooltipTemplates = sortedMapOf<Int, (RootGUIElement) -> String?>()

    private var window = GUIWindow("Mouse", { xPixel }, { yPixel }, { 0 }, { 0 }, true, 0, ScreenManager.Groups.MOUSE).apply {
        transparentToInteraction = true
    }
    private var group = GUIGroup(window.rootChild, "Mouse info group", { 0 }, { 0 }, open = true).apply {
        transparentToInteraction = true
    }

    private var text = GUIText(group, "Mouse info group text", 2, 2, "", layer = group.layer + 3).apply {
        open = false
    }

    private var background = GUITexturePane(group, "Mouse info group background", 0, 0, Image(Utils.genRectangle(text.widthPixels + 4, text.heightPixels + 4)), layer = group.layer + 2).apply {
        open = false
    }
    private var icon = GUITexturePane(group, "Mouse icon", 0, 0, Image.Misc.ERROR, ICON_SIZE, ICON_SIZE).apply {
        open = false
        updateDimensionAlignmentOnTextureChange = false
    }
    init {
        InputManager.registerControlPressHandler(this, ControlPressHandlerType.GLOBAL, Control.DROP_HELD_ITEM, Control.PICK_UP_DROPPED_ITEMS)
    }

    fun setSecondaryIcon(texture: Texture) {
        icon.texture = texture
        icon.open = true
    }

    fun setPrimaryIcon(texture: Texture) {
    }

    fun clearIcon() {
        icon.open = false
    }

    fun addLevelTooltipTemplate(f: (LevelObject) -> String?, priority: Int = 0) {
        levelTooltipTemplates.put(priority, f)
    }

    fun addScreenTooltipTemplate(f: (RootGUIElement) -> String?, priority: Int = 0) {
        screenTooltipTemplates.put(priority, f)
    }

    fun update() {
        var s: String? = null
        val screen = ScreenManager.getHighestElement(xPixel, yPixel, ScreenManager.getHighestWindow(xPixel, yPixel, { !it.transparentToInteraction }), { !it.transparentToInteraction })
        if (screen != null) {
            for (v in screenTooltipTemplates.values) {
                s = v(screen)
                if (s != null)
                    break
            }
        }
        if (s == null && State.CURRENT_STATE == State.INGAME) {
            val level = Game.currentLevel.selectedLevelObject
            if (level != null) {
                for (v in levelTooltipTemplates.values) {
                    s = v(level)
                    if (s != null)
                        break
                }
            }
        }
        if (s != null) {
            text.text = s
            text.open = true
            background.texture = Image(Utils.genRectangle(text.widthPixels + 4, text.heightPixels + 4))
            background.open = true
        } else {
            text.open = false
            background.open = false
        }
        window.updateAlignment()
    }

    fun render() {
        if (heldItemType != null) {
            val i = heldItemType!!
            val q = Game.mainInv.getQuantity(i)
            var w = GUIItemSlot.WIDTH
            var h = GUIItemSlot.HEIGHT
            val t = i.texture
            if (t.widthPixels > t.heightPixels) {
                if (t.widthPixels > GUIItemSlot.WIDTH) {
                    w = GUIItemSlot.WIDTH
                    val ratio = GUIItemSlot.WIDTH.toDouble() / t.widthPixels
                    h = (t.heightPixels * ratio).toInt()
                }
            }
            if (t.heightPixels > t.widthPixels) {
                if (t.heightPixels > GUIItemSlot.HEIGHT) {
                    h = GUIItemSlot.HEIGHT
                    val ratio = GUIItemSlot.HEIGHT.toDouble() / t.heightPixels
                    w = (t.widthPixels * ratio).toInt()
                }
            }
            Renderer.renderTexture(t, xPixel + (GUIItemSlot.WIDTH - w) / 2, yPixel + (GUIItemSlot.HEIGHT - h) / 2, w, h)
            Renderer.renderText(q, xPixel, yPixel)
        }
        if (Game.DEBUG_TUBE_INFO) {
            val t = Game.currentLevel.selectedLevelObject
            if (t is TubeBlock) {
                val tubeString = "Tube:\n" +
                        "  Tile: ${t.xTile}, ${t.yTile}\n" +
                        "  Group: ${t.group.id}\n"
                val intersection = t.group.intersections.firstOrNull { it.tubeBlock == t }
                val intersectionString =
                        if (t.group.isIntersection(t) == true && intersection != null)
                            "Intersection connections:\n" +
                                    "  Up: ${intersection.connectedTo[0]?.dist}\n" +
                                    "  Right: ${intersection.connectedTo[1]?.dist}\n" +
                                    "  Down: ${intersection.connectedTo[2]?.dist}\n" +
                                    "  Left: ${intersection.connectedTo[3]?.dist}\n"
                        else if (t.group.isIntersection(t) && intersection == null)
                            "Should be intersection but hasn't been added"
                        else "Not intersection\n"
                Renderer.renderText(tubeString + intersectionString, xPixel, yPixel)
            }
        } else if(Game.RESOURCE_NODES_INFO) {
            val nodes = Level.ResourceNodes.get(Game.currentLevel.mouseLevelXPixel shr 4, Game.currentLevel.mouseLevelYPixel shr 4)
            val s = StringBuilder()
            for(n in nodes) {
                s.append("    in: ${n.allowIn}, out: ${n.allowOut}, dir: ${n.dir}\n")
            }
            Renderer.renderText("Resource nodes:\n$s", xPixel, yPixel)
        } else if (Game.DEBUG_SCREEN_INFO) {
            Renderer.renderText("Element on mouse:\n" +
                    "  ${ScreenManager.getHighestElement(xPixel, yPixel, predicate = { !it.transparentToInteraction })}\n" +
                    "Window on mouse:\n" +
                    "  ${ScreenManager.getHighestWindow(xPixel, yPixel, { !it.transparentToInteraction })}", xPixel, yPixel)
        } else if (DebugOverlay.open) {
            Renderer.renderText("Screen:\n" +
                    "  Pixel: $xPixel, $yPixel\n" +
                    "  Tile: ${xPixel shr 4}, ${yPixel shr 4}\n" +
                    "  Chunk: ${xPixel shr CHUNK_PIXEL_EXP}, ${yPixel shr CHUNK_PIXEL_EXP}\n" +
                    if (State.CURRENT_STATE == State.INGAME) "Level:\n" +
                            "  Pixel: ${Game.currentLevel.mouseLevelXPixel}, ${Game.currentLevel.mouseLevelYPixel}\n" +
                            "  Tile: ${Game.currentLevel.mouseLevelXPixel shr 4}, ${Game.currentLevel.mouseLevelYPixel shr 4}\n" +
                            "  Chunk: ${Game.currentLevel.mouseLevelXPixel shr CHUNK_PIXEL_EXP}, ${Game.currentLevel.mouseLevelYPixel shr CHUNK_PIXEL_EXP}" else "", xPixel, yPixel)
        }
    }

    fun dropHeldItem(q: Int = 1) {
        if (heldItemType != null) {
            val type = heldItemType!!
            if (Level.add(DroppedItem(Game.currentLevel.mouseLevelXPixel, Game.currentLevel.mouseLevelYPixel, type, q)))
                Game.mainInv.remove(type, q)
        }
    }

    override fun onContainerAdd(container: ResourceContainer<*>, resource: ResourceType, quantity: Int) {
    }

    override fun onContainerRemove(inv: Inventory, resource: ResourceType, quantity: Int) {
    }

    override fun onContainerClear(container: ResourceContainer<*>) {
    }

    override fun onContainerChange(container: ResourceContainer<*>) {
        if(container == Game.mainInv && heldItemType != null) {
            if(Game.mainInv.getQuantity(heldItemType!!) == 0)
                heldItemType = null
        }
    }

    override fun handleControlPress(p: ControlPress) {
        if (p.pressType == PressType.PRESSED) {
            when (p.control) {
                Control.DROP_HELD_ITEM -> dropHeldItem()
                Control.PICK_UP_DROPPED_ITEMS -> {
                    val i = Level.DroppedItems.getInRadius(Game.currentLevel.mouseLevelXPixel, Game.currentLevel.mouseLevelYPixel, DROPPED_ITEM_PICK_UP_RANGE)
                    if (i.isNotEmpty()) {
                        val g = i.first()
                        if (!Game.mainInv.full) {
                            Game.mainInv.add(g.type, g.quantity)
                            Level.remove(g)
                            if(heldItemType == null) {
                                heldItemType = g.type
                            }
                            HUD.Hotbar.items.add(g.type)
                        }
                    }
                }
            }
        }
    }
}
