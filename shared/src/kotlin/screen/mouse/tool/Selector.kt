package screen.mouse.tool

import graphics.Renderer
import graphics.TextureRenderParams
import io.Control
import io.PressType
import level.*
import level.block.Block
import level.entity.Entity
import level.moving.MovingObject
import main.toColor
import network.MovingObjectReference
import network.packet.Packet
import network.packet.PacketHandler
import player.CreateEntityGroup
import player.PlayerManager
import screen.mouse.Mouse

private enum class SelectorMode {
    ALL, ADD, SUBTRACT
}

object Selector : Tool(Control.Group.SELECTOR_TOOLS), PacketHandler {

    private const val SELECTION_START_THRESHOLD = 2

    var startPress = false
    var dragging = false
    var dragStartXPixel = 0
    var dragStartYPixel = 0
    var currentDragXPixel = 0
    var currentDragYPixel = 0

    private var mode = SelectorMode.ALL

    var currentSelected = mutableSetOf<LevelObject>()
    var newlySelected = mutableSetOf<LevelObject>()

    override fun handleServerPacket(packet: Packet) {
    }

    override fun handleClientPacket(packet: Packet) {
    }

    override fun update() {
        currentSelected.removeIf { !it.inLevel }
        newlySelected.removeIf { !it.inLevel }
    }

    override fun updateCurrentlyActive() {
        currentlyActive = Mouse.heldItemType == null
    }

    override fun onUse(control: Control, type: PressType, mouseLevelXPixel: Int, mouseLevelYPixel: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean) {
        when (control) {
            Control.START_SELECTION -> mode = SelectorMode.ALL
            Control.START_SELECTION_ADD -> mode = SelectorMode.ADD
            Control.START_SELECTION_SUBTRACT -> mode = SelectorMode.SUBTRACT
        }
        if (type == PressType.PRESSED) {
            startPress = true
            if (mode == SelectorMode.ALL) {
                currentSelected = mutableSetOf()
            }
            newlySelected = mutableSetOf()
            dragStartXPixel = mouseLevelXPixel
            dragStartYPixel = mouseLevelYPixel
            currentDragXPixel = mouseLevelXPixel
            currentDragYPixel = mouseLevelYPixel
        } else if (type == PressType.REPEAT) {
            if (startPress) {
                currentDragXPixel = mouseLevelXPixel
                currentDragYPixel = mouseLevelYPixel
                if (Math.abs(dragStartXPixel - currentDragXPixel) > SELECTION_START_THRESHOLD || Math.abs(dragStartYPixel - currentDragYPixel) > SELECTION_START_THRESHOLD) {
                    dragging = true
                    updateSelected()
                }
            }
        } else if (type == PressType.RELEASED) {
            if (dragging) {
                updateSelected()
                currentSelected = when (mode) {
                    SelectorMode.ADD -> currentSelected + newlySelected
                    SelectorMode.ALL -> newlySelected
                    SelectorMode.SUBTRACT -> currentSelected - newlySelected
                }.toMutableSet()
                val entitiesSelected = currentSelected.filterIsInstance<Entity>()
                if (entitiesSelected.isNotEmpty()) {
                    PlayerManager.takeAction(CreateEntityGroup(PlayerManager.localPlayer, entitiesSelected.map { it.toReference() as MovingObjectReference }))
                }
                newlySelected = mutableSetOf()
                dragging = false
            }
            startPress = false
        }
    }

    private fun updateSelected() {
        if (dragging) {
            val xChange = currentDragXPixel - dragStartXPixel
            val yChange = currentDragYPixel - dragStartYPixel
            val x = if (xChange < 0) currentDragXPixel else dragStartXPixel
            val y = if (yChange < 0) currentDragYPixel else dragStartYPixel
            val w = Math.abs(xChange)
            val h = Math.abs(yChange)
            newlySelected =
                    (LevelManager.levelUnderMouse!!.getIntersectingBlocksFromPixelRectangle(x, y, w, h).toList() +
                            LevelManager.levelUnderMouse!!.getMovingObjectCollisionsFromPixelRectangle(x, y, w, h)).toMutableSet()
        }
    }

    override fun renderBelow() {
        if (dragging) {
            Renderer.renderEmptyRectangle(dragStartXPixel, dragStartYPixel, (currentDragXPixel - dragStartXPixel), (currentDragYPixel - dragStartYPixel), params = TextureRenderParams(color = toColor(0.8f, 0.8f, 0.8f)))
        }
        val selection = if (dragging) when (mode) {
            SelectorMode.ADD -> currentSelected + newlySelected
            SelectorMode.ALL -> newlySelected
            SelectorMode.SUBTRACT -> currentSelected - newlySelected
        } else currentSelected
        for (l in selection) {
            if (l is Block) {
                Renderer.renderEmptyRectangle(l.xPixel - 1, l.yPixel - 1, (l.type.widthTiles shl 4) + 2, (l.type.heightTiles shl 4) + 2, params = TextureRenderParams(color = toColor(alpha = 0.2f)))
            } else if (l is MovingObject && l.hitbox != Hitbox.NONE) {
                Renderer.renderEmptyRectangle(l.xPixel + l.hitbox.xStart - 1, l.yPixel + l.hitbox.yStart - 1, l.hitbox.width + 2, l.hitbox.height + 2, params = TextureRenderParams(color = toColor(alpha = 0.2f)))
            }
        }
    }
}