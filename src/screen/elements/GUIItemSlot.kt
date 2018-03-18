package screen.elements

import graphics.Image
import graphics.Renderer
import io.PressType
import item.Inventory
import item.Item
import item.ItemType
import main.Game
import resource.ResourceContainer
import screen.HUD
import screen.InventoryGUI
import screen.Mouse
import screen.ScreenManager


class GUIItemSlot(parent: RootGUIElement, name: String, xPixel: Int, yPixel: Int, var index: Int, var inv: ResourceContainer<ItemType>,
                  var isDisplay: Boolean = false, open: Boolean = false, layer: Int = parent.layer + 1) :
        GUIElement(parent, name, xPixel, yPixel, WIDTH, HEIGHT, open, layer) {

    private var currentTexture = if (isDisplay) Image.GUI.ITEM_SLOT_DISPLAY else Image.GUI.ITEM_SLOT

    var currentItem: Item? = null

    override fun update() {
        if (inv is Inventory) {
            currentItem = (inv as Inventory)[index]
        } else if (inv is HUD.Hotbar.HotbarInventory) {
            val invItemType = (inv as HUD.Hotbar.HotbarInventory)[index]
            if (invItemType != null) {
                currentItem = Item(invItemType, Game.mainInv.getQuantity(invItemType))
            } else {
                currentItem = null
            }
        }

    }

    override fun render() {
        Renderer.renderTexture(currentTexture, xPixel, yPixel)
        if (currentItem != null) {
            val i = currentItem!!
            Renderer.renderTextureKeepAspect(i.type.texture, xPixel, yPixel, WIDTH, HEIGHT)
            Renderer.renderText(i.quantity, xPixel, yPixel)
        }
    }

    override fun onMouseEnter() {
        if (!isDisplay)
            currentTexture = Image.GUI.ITEM_SLOT_HIGHLIGHT
    }

    override fun onMouseLeave() {
        if (!isDisplay)
            currentTexture = Image.GUI.ITEM_SLOT
    }

    override fun onClose() {
        currentTexture = Image.GUI.ITEM_SLOT
    }

    override fun onMouseActionOn(type: PressType, xPixel: Int, yPixel: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean) {
        if (isDisplay)
            return
        if (type == PressType.PRESSED) {
            HUD.Hotbar.selected = -1
            currentTexture = Image.GUI.ITEM_SLOT_CLICK
            if (shift) {
                if (currentItem != null) {
                    val i = currentItem!!
                    val other = getSecondaryInventory()
                    if (other != null) {
                        other.add(i)
                        inv.remove(i.type, i.quantity)
                    } else {
                        HUD.Hotbar.items.add(i.type)
                    }
                }
            } else if (ctrl) {
                if (currentItem != null) {
                    val i = currentItem!!
                    val other = getSecondaryInventory()
                    if (other != null) {
                        val q = inv.getQuantity(i.type)
                        other.add(i.type, q)
                        inv.remove(i.type, q)
                    }
                }
            } else {
                val cI = currentItem
                if (button == 1) {
                    Mouse.heldItemType = cI?.type
                }
            }
        } else if (type == PressType.RELEASED) {
            currentTexture = Image.GUI.ITEM_SLOT
        }
    }

    private fun getSecondaryInventory(): Inventory? {
        val invGUIs = ScreenManager.Groups.INVENTORY.windows
        if (invGUIs.isNotEmpty()) {
            val highestOtherWindow = invGUIs.filter { it.open && it != parentWindow }.maxBy { it.layer }
            if (highestOtherWindow != null && highestOtherWindow is InventoryGUI) {
                return highestOtherWindow.inv
            }
        }
        return null
    }

    companion object {
        const val WIDTH = 16
        const val HEIGHT = 16

        init {
            Mouse.addScreenTooltipTemplate({
                if (it is GUIItemSlot && it.currentItem != null) {
                    return@addScreenTooltipTemplate "${it.currentItem!!.type.name} * ${it.currentItem!!.quantity}"
                }
                return@addScreenTooltipTemplate null
            }, 0)
        }
    }
}