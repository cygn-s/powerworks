package level.block

import com.badlogic.gdx.Input
import io.ControlEvent
import io.ControlEventType
import item.Inventory
import item.OreItemType
import resource.ResourceContainer
import resource.ResourceContainerChangeListener
import resource.ResourceList
import resource.resourceListOf
import serialization.Id

class SmelterBlock(xTile: Int, yTile: Int, rotation: Int) : MachineBlock(MachineBlockType.SMELTER, xTile, yTile, rotation), ResourceContainerChangeListener {
    @Id(23)
    val input: Inventory = nodes.first { it.behavior.allowIn.statements.keys.first().text == "true" }.attachedContainer as Inventory

    @Id(24)
    val output: Inventory = nodes.first { it.behavior.allowOut.statements.keys.first().text == "true" }.attachedContainer as Inventory

    @Id(25)
    var currentlySmelting: OreItemType? = null

    init {
        containers.forEach { it.listeners.add(this) }
    }

    override fun onContainerClear(container: ResourceContainer) {
        if (container.id == input.id) {
            currentlySmelting = null
            on = false
        }
    }

    override fun onAddToContainer(container: ResourceContainer, resources: ResourceList) {
        if (container.id == input.id) {
            if (currentlySmelting == null) {
                currentlySmelting = resources[0].key as OreItemType
                on = true
            }
        }
    }

    override fun onRemoveFromContainer(container: ResourceContainer, resources: ResourceList) {
        if (container.id == input.id) {
            if (currentlySmelting == null) {
                currentlySmelting = resources[0].key as OreItemType
                on = true
            }
        }
    }

    override fun update() {
        on = input.totalQuantity > 1 && !(output[0] != null && output[0]!!.quantity >= output[0]!!.type.maxStack)
        super.update()
    }

    override fun onFinishWork() {
        if (currentlySmelting == null) {
            // some desync
            return
        }
        if (output.canAddAll(resourceListOf(currentlySmelting!!.moltenForm.ingot to 1))) {
            if (input.remove(currentlySmelting!!, 2)) {
                output.add(currentlySmelting!!.moltenForm.ingot, 1, checkIfAble = false)
            }
        }
        if (input.totalQuantity > 1) {
            // start smelting another item
            if (input.getQuantity(currentlySmelting!!) == 0) {
                currentlySmelting = input.toResourceList()[0].key as OreItemType
            }
            // do nothing, old item still has quantity
        } else {
            on = false
            currentlySmelting = null
        }
    }

    override fun onInteractOn(event: ControlEvent, x: Int, y: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean) {
        if (event.type == ControlEventType.PRESS && !shift && !ctrl && !alt) {
            if (button == Input.Buttons.LEFT) {
                this.type.guiPool!!.toggle(this)
            }
        }
    }
}