package screen

import crafting.Recipe
import graphics.Image
import graphics.Utils
import io.PressType
import screen.elements.*

/**
 * A handy little thing that any class can open up and accept a choice from
 * The best way to get the choice is calling getSelected() in your update method
 */
object RecipeSelectorGUI : GUIWindow("Recipe selector", 20, 20, 100, 120, windowGroup = ScreenManager.Groups.PLAYER_UTIL) {

    private const val RECIPIES_PER_ROW = 6

    private var selected: Recipe? = null

    init {
        openAtMouse = true
        partOfLevel = true
        val background = GUIDefaultTextureRectangle(this.rootChild, "Background", 0, 0)
        GUIText(background, "Name text", 0, 0, "Select a recipe:")
        for ((i, recipe) in Recipe.ALL.withIndex()) {
            val display = GUIRecipeDisplay(background, "Recipe $i display", {(i % RECIPIES_PER_ROW) * GUIRecipeDisplay.WIDTH + 1}, {(i / RECIPIES_PER_ROW) * GUIRecipeDisplay.HEIGHT + 6}, recipe)
            GUIClickableRegion(display, "Recipe $i click region", { 0 }, { 0 }, { GUIRecipeDisplay.WIDTH }, { GUIRecipeDisplay.HEIGHT }, { pressType, _, _, button, shift, ctrl, alt ->
                if (pressType == PressType.PRESSED && button == 1) {
                    selected = recipe
                }
            }, layer = display.layer + 2)
        }
        generateCloseButton(background.layer + 1)
        generateDragGrip(background.layer + 1)
    }

    /**
     * Gets the last selected recipe
     *
     * *NOTE*: It clears itself after you call this and it returns non-null
     */
    fun getSelected(): Recipe? {
        if (selected != null) {
            val s = selected
            selected = null
            return s
        }
        return null
    }
}