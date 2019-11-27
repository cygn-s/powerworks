package level

import graphics.Renderer
import io.*
import item.Item
import item.ItemType
import level.moving.MovingObject
import level.moving.MovingObjectType
import main.DebugCode
import main.Game
import screen.HUD
import screen.mouse.Mouse
import screen.mouse.Tooltips
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag
import player.PlayerManager

class DroppedItem(xPixel: Int, yPixel: Int,
                  @Tag(1)
                  val itemType: ItemType,
                  quantity: Int = 1) :
        MovingObject(MovingObjectType.DROPPED_ITEM, xPixel, yPixel, 0) {

    @Tag(2)
    var quantity = quantity
        set(value) {
            if (quantity < 1) {
                level.remove(this)
            } else {
                field = value
            }
        }

    override fun onInteractOn(type: PressType, xPixel: Int, yPixel: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean) {
        if (type == PressType.RELEASED) {
            level.remove(this)
            PlayerManager.localPlayer.brainRobot.inventory.add(Item(itemType, quantity))
            Mouse.heldItemType = itemType
            HUD.Hotbar.items.add(itemType)
        }
    }

    override fun render() {
        itemType.icon.render(xPixel, yPixel, Hitbox.DROPPED_ITEM.width, Hitbox.DROPPED_ITEM.height, true)
        Renderer.renderText(quantity, xPixel, yPixel)
        if(Game.currentDebugCode == DebugCode.RENDER_HITBOXES) {
            renderHitbox()
        }
    }

    override fun toString() = "Dropped item at $xPixel, $yPixel, type: $type, quantity: $quantity"

    companion object {
        init {
            Tooltips.addLevelTooltipTemplate({
                if (it is DroppedItem)
                    return@addLevelTooltipTemplate "${it.itemType.name} * ${it.quantity}"
                return@addLevelTooltipTemplate null
            })
        }
    }
}