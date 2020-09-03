package screen.element

import graphics.Renderer
import graphics.TextureRenderParams
import graphics.text.TextManager
import graphics.text.TextRenderParams
import item.Inventory
import main.toColor
import resource.ResourceContainer
import resource.ResourceContainerChangeListener
import resource.ResourceList
import resource.ResourceType
import screen.gui.GuiElement
import screen.Interaction

open class ElementResourceContainer(parent: GuiElement, width: Int, height: Int,
                                    container: ResourceContainer,
                                    allowSelection: Boolean = false,
                                    onSelect: (type: ResourceType, quantity: Int, interaction: Interaction) -> Unit = { _, _, _ -> }) :
        ElementIconList(parent, width, height, allowSelection = allowSelection, renderIcon = { _, _, _ -> }), ResourceContainerChangeListener {

    open var container = container
        set(value) {
            if (field.id != value.id) {
                field.listeners.remove(this)
                field = value
                if (value is Inventory) {
                    columns = value.width
                    rows = value.height
                }
                currentResources = container.toResourceList()
                value.listeners.add(this)
            }
        }

    var currentResources = container.toResourceList()
        private set

    init {
        onSelectIcon = { index, interaction ->
            val resources = this.container.toResourceList()
            if (index < resources.size) {
                val (type, quantity) = resources[index]
                onSelect(type, quantity, interaction)
            }
        }
        renderIcon = { x, y, index -> renderIconAt(x, y, index) }
        getToolTip = { index ->
            if (index < currentResources.size) {
                val entry = currentResources[index]
                "${entry.key.name} * ${entry.value}"
            } else if (index - currentResources.size < container.expected.size) {
                val expected = container.expected[index - currentResources.size]
                "${expected.key.name} * ${expected.value}"
            } else {
                null
            }
        }
        container.listeners.add(this)
    }

    private fun renderIconAt(x: Int, y: Int, index: Int) {
        if (index < currentResources.size) {
            val entry = currentResources[index]
            entry.key.icon.render(x, y, iconSize, iconSize, true)
            Renderer.renderText(entry.value, x, y)
            val width = TextManager.getStringWidth(entry.value.toString())
            val expectedOfType = container.expected[entry.key]
            if (expectedOfType != 0) {
                Renderer.renderText("(+$expectedOfType)", x + width, y, TextRenderParams(size = 15))
            }
        } else {
            val expected = container.expected
            if (index - currentResources.size < expected.size) {
                val (type, quantity) = expected[index - currentResources.size]
                type.icon.render(x, y, iconSize, iconSize, true, TextureRenderParams(color = toColor(a = 0.6f)))
                Renderer.renderText(quantity, x, y)
            }
        }
    }

    override fun onContainerClear(container: ResourceContainer) {
        currentResources.clear()
    }

    override fun onAddToContainer(container: ResourceContainer, resources: ResourceList) {
        currentResources.addAll(resources)
    }

    override fun onRemoveFromContainer(container: ResourceContainer, resources: ResourceList) {
        currentResources.takeAll(resources)
    }
}