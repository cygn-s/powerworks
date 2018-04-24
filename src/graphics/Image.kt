package graphics

import main.Game
import main.ResourceManager
import screen.elements.GUIRecipeDisplay
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Rectangle
import java.awt.Transparency
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Utils {

    private val rectTable = mutableMapOf<Rectangle, BufferedImage>()

    /**
     * @return an image of a rectangle using the default parts in res/textures/gui/default
     */
    fun genRectangle(widthPixels: Int, heightPixels: Int): BufferedImage {
        return genRectangle(Image.GUI.DEFAULT_EDGE_TOP, Image.GUI.DEFAULT_EDGE_BOTTOM, Image.GUI.DEFAULT_EDGE_LEFT, Image.GUI.DEFAULT_EDGE_RIGHT, Image.GUI.DEFAULT_CORNER_TOP_RIGHT,
                Image.GUI.DEFAULT_CORNER_TOP_LEFT, Image.GUI.DEFAULT_CORNER_BOTTOM_RIGHT, Image.GUI.DEFAULT_CORNER_BOTTOM_LEFT, Image.GUI.DEFAULT_BACKGROUND, widthPixels, heightPixels)
    }

    /**
     * @return an image of a rectangle using the textures specified
     */
    fun genRectangle(topEdge: Texture, bottomEdge: Texture, leftEdge: Texture, rightEdge: Texture, topRightCorner: Texture, topLeftCorner: Texture, bottomRightCorner: Texture,
                     bottomLeftCorner: Texture, background: Texture, widthPixels: Int, heightPixels: Int): BufferedImage {
        val rect = Rectangle(widthPixels, heightPixels)
        val previous = rectTable[rect]
        if(previous != null) {
            return previous
        }
        val dest = Game.graphicsConfiguration.createCompatibleImage(widthPixels, heightPixels, Transparency.TRANSLUCENT)
        val g = dest.createGraphics()
        with(g) {
            composite = AlphaComposite.SrcOver
            for (x in 0 until widthPixels step background.widthPixels) {
                for (y in 0 until heightPixels step background.heightPixels) {
                    drawImage(background.currentImage, x, y, null)
                }
            }
            for (i in topLeftCorner.widthPixels until widthPixels - topRightCorner.widthPixels step topEdge.widthPixels)
                drawImage(topEdge.currentImage, i, 0, null)
            for (i in topRightCorner.heightPixels until heightPixels - bottomRightCorner.heightPixels step rightEdge.heightPixels)
                drawImage(rightEdge.currentImage, widthPixels - rightEdge.widthPixels, i, null)
            for (i in bottomLeftCorner.widthPixels until widthPixels - bottomRightCorner.widthPixels step bottomEdge.widthPixels)
                drawImage(bottomEdge.currentImage, i, heightPixels - bottomEdge.heightPixels, null)
            for (i in topLeftCorner.heightPixels until heightPixels - bottomLeftCorner.heightPixels step leftEdge.heightPixels)
                drawImage(leftEdge.currentImage, 0, i, null)
            drawImage(topLeftCorner.currentImage, 0, 0, null)
            drawImage(topRightCorner.currentImage, widthPixels - topRightCorner.widthPixels, 0, null)
            drawImage(bottomRightCorner.currentImage, widthPixels - bottomRightCorner.widthPixels, heightPixels - bottomRightCorner.heightPixels, null)
            drawImage(bottomLeftCorner.currentImage, 0, heightPixels - bottomLeftCorner.heightPixels, null)
            dispose()
        }
        rectTable.put(rect, dest)
        return dest
    }

    /**
     * @param i the original image
     * @return a BufferedImage object modified according to the ImageParams object
     */
    fun modify(i: Image, p: ImageParams): BufferedImage {
        return modify(i.currentImage, p)
    }

    /**
     * @param path the path to the original image
     * @return a BufferedImage object modified according to the ImageParams object
     */
    fun modify(path: String, p: ImageParams): BufferedImage {
        return modify(loadImage(path), p)
    }

    /**
     * @param image the original image
     * @return a BufferedImage object modified according to the ImageParams object
     */
    fun modify(image: BufferedImage, p: ImageParams): BufferedImage {
        var newImg: BufferedImage = image
        if (p.scale != 1.0) {
            newImg = Game.graphicsConfiguration.createCompatibleImage((image.width * p.scale).toInt(), (image.height * p.scale).toInt(), Transparency.TRANSLUCENT)
            val g2d = newImg.createGraphics()
            with(g2d) {
                drawImage(image, 0, 0, (image.width * p.scale).toInt(), (image.height * p.scale).toInt(), null)
                dispose()
            }
        }
        if (p.scaleWidth != 1.0) {
            newImg = Game.graphicsConfiguration.createCompatibleImage((image.width * p.scaleWidth).toInt(), image.height, Transparency.TRANSLUCENT)
            val g2d = newImg.createGraphics()
            with(g2d) {
                drawImage(image, 0, 0, (image.width * p.scaleWidth).toInt(), image.height, null)
                dispose()
            }
        }
        if (p.scaleHeight != 1.0) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, (image.height * p.scaleHeight).toInt(), Transparency.TRANSLUCENT)
            val g2d = newImg.createGraphics()
            with(g2d) {
                drawImage(image, 0, 0, image.width, (image.height * p.scaleHeight).toInt(), null)
                dispose()
            }
        }
        if (p.alphaMultiplier != -1) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y), true)
                    val newC = Color(c.red, c.green, c.blue, p.alphaMultiplier * (c.alpha / 255))
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.toRed) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(c.red, 0, 0, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.toGreen) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(0, c.green, 0, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.toBlue) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(0, 0, c.blue, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.redValue != -1) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(p.redValue, c.green, c.blue, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.greenValue != -1) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(c.red, p.greenValue, c.blue, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.blueValue != -1) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            for (y in 0..image.height - 1) {
                for (x in 0..image.width - 1) {
                    val c = Color(image.getRGB(x, y))
                    val newC = Color(c.red, c.green, p.blueValue, c.rgb shr 24 and 0xFF)
                    newImg.setRGB(x, y, newC.rgb)
                }
            }
        }
        if (p.rotation != 0) {
            if (p.rotation == 1 || p.rotation == 3) {
                newImg = Game.graphicsConfiguration.createCompatibleImage(image.height, image.width, Transparency.TRANSLUCENT)
                val g2d = newImg.createGraphics()
                with(g2d) {
                    rotate(Math.toRadians(p.rotation.toDouble() * 90), image.width.toDouble() / 2, image.height.toDouble() / 2)
                    drawImage(image, 0, 0, null)
                    dispose()
                }
            } else {
                newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
                val g2d = newImg.createGraphics()
                with(g2d) {
                    rotate(Math.toRadians(180.0), image.width.toDouble() / 2, image.height.toDouble() / 2)
                    drawImage(image, 0, 0, null)
                    dispose()
                }
            }
        }
        if (p.brightnessMultiplier != 1.0) {
            newImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            val tempImg = Game.graphicsConfiguration.createCompatibleImage(image.width, image.height, Transparency.TRANSLUCENT)
            tempImg.createGraphics().drawImage(image, 0, 0, null)
            val wr = tempImg.raster
            val pixel = IntArray(4)
            for (i in 0 until wr.width) {
                for (j in 0 until wr.height) {
                    wr.getPixel(i, j, pixel)
                    pixel[0] = (pixel[0] * p.brightnessMultiplier).toInt()
                    pixel[1] = (pixel[1] * p.brightnessMultiplier).toInt()
                    pixel[2] = (pixel[2] * p.brightnessMultiplier).toInt()
                    wr.setPixel(i, j, pixel)
                }
            }
            newImg.createGraphics().drawImage(tempImg, 0, 0, null)
        }
        return newImg
    }

    /**
     * @return a BufferedImage object read from the destination, path relative to the inside of the jarfile
     */
    fun loadImage(path: String): BufferedImage {
        val src = ImageIO.read(ResourceManager.getResource(path))
        val dest = Game.graphicsConfiguration.createCompatibleImage(src.width, src.height, Transparency.TRANSLUCENT)
        val g2d = dest.createGraphics()
        g2d.composite = AlphaComposite.SrcOver
        g2d.run {
            drawImage(src, 0, 0, null)
            dispose()
        }
        return dest
    }
}

data class ImageParams(
        /** 1.0 is default, 2.0 is double size, 0.5 is half size */
        val scale: Double = 1.0, val scaleWidth: Double = 1.0, val scaleHeight: Double = 1.0,
        /** -1 is default, 0 is totally translucent, 255 is totally opaque. Note this is a multiplier, not a universal setter, so previously transparent pixels will not be affected */
        val alphaMultiplier: Int = -1,
        /** false is default, true sets all other color bands to 0 and retains this color */
        val toRed: Boolean = false, val toGreen: Boolean = false, val toBlue: Boolean = false,
        /** -1 is default, 255 is max of this color and 0 removes this color. Note this is not a multiplier*/
        val redValue: Int = -1, val greenValue: Int = -1, val blueValue: Int = -1,
        /** 0 is default, 1 is 90 degrees, 2 is 180, etc.*/
        val rotation: Int = 0,
        /** 1.0 is default, 2.0 is double brightness and 0.5 is half brightness*/
        val brightnessMultiplier: Double = 1.0)

class WeaponImages internal constructor(id: Int) {

}

class Image private constructor() : Texture {

    override val widthPixels: Int
        get() = currentImage.width
    override val heightPixels: Int
        get() = currentImage.height

    override lateinit var currentImage: BufferedImage

    constructor(path: String) : this() {
        currentImage = Utils.loadImage(path)
    }

    constructor(image: BufferedImage) : this() {
        this.currentImage = image
    }

    constructor(color: Color) : this() {
        currentImage = Game.graphicsConfiguration.createCompatibleImage(1, 1, Transparency.TRANSLUCENT)
        val g2d = currentImage.createGraphics()
        with(g2d) {
            g2d.color = color
            fillRect(0, 0, 1, 1)
            dispose()
        }
    }

    object Misc {
        // TODO definitely rethink how weapon textures are done, apply this to other things in the future like block textures
        val ERROR = Image("/textures/misc/error.png")
        val ARROW = Image(Utils.modify("/textures/misc/arrow.png", ImageParams(alphaMultiplier = 100)))
        val VOID = Image(Color(0))
        val TELEPORT_ICON = Image("/textures/misc/teleport_icon.png")
        val THIN_ARROW = Image("/textures/misc/thin_arrow.png")
    }

    object Block {
        val CHEST_SMALL = Image("/textures/block/chest_small.png")
        val CHEST_LARGE = Image("/textures/block/chest_large.png")
        val CRAFTER = Image("/textures/block/crafter_2.png")
        val TUBE_4_WAY = Image("/textures/block/tube/4_way.png")
        val TUBE_2_WAY_VERTICAL = Image("/textures/block/tube/2_way_vertical.png")
        val TUBE_2_WAY_HORIZONTAL = Image("/textures/block/tube/2_way_horizontal.png")
        val TUBE_UP_CLOSE = Image("/textures/block/tube/up_close.png")
        val TUBE_RIGHT_CLOSE = Image("/textures/block/tube/right_close.png")
        val TUBE_DOWN_CLOSE = Image("/textures/block/tube/down_close.png")
        val TUBE_LEFT_CLOSE = Image("/textures/block/tube/left_close.png")
    }

    object GUI {
        val VIEW_SELECTOR_CLOSE_BUTTON = Image("/textures/gui/view_selector_close.png")
        val VIEW_SELECTOR_CLOSE_BUTTON_HIGHLIGHT = Image("/textures/gui/view_selector_close_highlight.png") // WTF is going on here, for some reason when I use Utils.modify it comes up as black and not red TODO
        val VIEW_SELECTOR_OPEN_BUTTON = Image("/textures/gui/view_selector_open.png")
        val VIEW_SELECTOR_OPEN_BUTTON_HIGHLIGHT = Image(Utils.modify(VIEW_SELECTOR_OPEN_BUTTON, ImageParams(brightnessMultiplier = 1.2)))
        val CLOSE_BUTTON = Image("/textures/gui/close_button.png")
        val DRAG_GRIP = Image("/textures/gui/drag_grip.png")
        val DIMENSION_DRAG_GRIP = Image("/textures/gui/dimension_drag_grip.png")
        val SCROLL_BAR_TOP = Image("/textures/gui/scroll_bar_top.png")
        val SCROLL_BAR_MIDDLE = Image("/textures/gui/scroll_bar_middle.png")
        val SCROLL_BAR_BOTTOM = Image("/textures/gui/scroll_bar_bottom.png")
        val SCROLL_BAR_UNHIGHLIGHT_TOP = Image("/textures/gui/scroll_bar_unhighlight_top.png")
        val SCROLL_BAR_UNHIGHLIGHT_MIDDLE = Image("/textures/gui/scroll_bar_unhighlight_mid.png")
        val SCROLL_BAR_UNHIGHLIGHT_BOTTOM = Image("/textures/gui/scroll_bar_unhighlight_bottom.png")
        val SCROLL_BAR_HIGHLIGHT_TOP = Image("/textures/gui/scroll_bar_highlight_top.png")
        val SCROLL_BAR_HIGHLIGHT_MIDDLE = Image("/textures/gui/scroll_bar_highlight_mid.png")
        val SCROLL_BAR_HIGHLIGHT_BOTTOM = Image("/textures/gui/scroll_bar_highlight_bottom.png")
        val SCROLL_BAR_CLICK_TOP = Image("/textures/gui/scroll_bar_click_top.png")
        val SCROLL_BAR_CLICK_MIDDLE = Image("/textures/gui/scroll_bar_click_mid.png")
        val SCROLL_BAR_CLICK_BOTTOM = Image("/textures/gui/scroll_bar_click_bottom.png")
        val MAIN_MENU_LOGO = Image("/textures/gui/main_menu_logo.png")
        val MAIN_MENU_LOGO_2 = Image("/textures/gui/main_menu_logo_2.png")
        val MAIN_MENU_LOGO_3 = Image("/textures/gui/main_menu_logo_3.png")
        val DEFAULT_CORNER_TOP_RIGHT = Image("/textures/gui/default/top_right_corner.png")
        val DEFAULT_CORNER_TOP_LEFT = Image("/textures/gui/default/top_left_corner.png")
        val DEFAULT_CORNER_BOTTOM_RIGHT = Image("/textures/gui/default/bottom_right_corner.png")
        val DEFAULT_CORNER_BOTTOM_LEFT = Image("/textures/gui/default/bottom_left_corner.png")
        val DEFAULT_EDGE_TOP = Image("/textures/gui/default/top_edge.png")
        val DEFAULT_EDGE_BOTTOM = Image("/textures/gui/default/bottom_edge.png")
        val DEFAULT_EDGE_RIGHT = Image("/textures/gui/default/right_edge.png")
        val DEFAULT_EDGE_LEFT = Image("/textures/gui/default/left_edge.png")
        val DEFAULT_BACKGROUND = Image("/textures/gui/default/background.png")
        val RESOURCE_DISPLAY_SLOT = Image("/textures/gui/resource_display_slot.png")
        val ITEM_SLOT = Image("/textures/gui/item_slot.png")
        val ITEM_SLOT_HIGHLIGHT = Image("/textures/gui/item_slot_highlight.png")
        val ITEM_SLOT_DISPLAY = Image("/textures/gui/item_slot_display.png")
        val ITEM_SLOT_CLICK = Image("/textures/gui/item_slot_click.png")
        val CHAT_BAR = Image(Utils.modify("/textures/gui/chat_bar.png", ImageParams(scaleWidth = 180.0, scaleHeight = 10.0)))
        val MAIN_MENU_BACKGROUND = Image("/textures/gui/main_menu_bg.png")
        val MAIN_MENU_BUTTON_BOX = Image("/textures/gui/main_menu_button_box.png")
        val OPTIONS_MENU_BACKGROUND = Image(Color(0x999999))
        val ESCAPE_MENU_BACKGROUND = Image("/textures/gui/escape_menu_background.png")
        val MAIN_MENU_BACKGROUND_FILLER = Image(Color(0x515151))
        val HOTBAR_SELECTED_SLOT = Image("/textures/gui/selected_slot.png")
        val CRAFTING_ARROW = Image("/textures/gui/crafting_arrow.png")
        val RECIPE_BUTTON_BACKGROUND = Image(Utils.genRectangle(GUIRecipeDisplay.WIDTH, GUIRecipeDisplay.HEIGHT))
    }

    object Item {
        val TUBE_ITEM = Image("/textures/item/tube.png")
        val COPPER_ORE_ITEM = Image("/textures/item/copper_ore_raw.png")
        val IRON_ORE_ITEM = Image("/textures/item/iron_ore_raw.png")
        val IRON_INGOT = Image("/textures/item/iron_ingot.png")
    }

}
