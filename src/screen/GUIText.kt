package screen

import graphics.Renderer
import main.Game
import java.awt.font.FontRenderContext

class GUIText(parent: RootGUIElement? = RootGUIElementObject,
              name: String,
              relXPixel: Int, relYPixel: Int,
              var text: String,
              var size: Int = 28,
              var color: Int = 0xffffff,
              layer: Int = (parent?.layer ?: 0) + 1) :
        GUIElement(parent, name, relXPixel, relYPixel, 0, 0, layer) {
    init {
        val r = Game.getFont(28).getStringBounds(text, FontRenderContext(null, false, false))
        widthPixels = (r.width / Game.SCALE).toInt()
        heightPixels = (r.height / Game.SCALE).toInt()
    }

    override fun render() {
        Renderer.renderText(text, xPixel, yPixel, size, color)
    }
}