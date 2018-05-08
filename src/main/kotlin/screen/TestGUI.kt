package screen

import graphics.Image
import main.Game
import screen.elements.GUIButton
import screen.elements.GUIElementList
import screen.elements.GUITexturePane
import screen.elements.GUIWindow

internal object TestGUI : GUIWindow("Testing GUI", 0, 0, Game.WIDTH, Game.HEIGHT, windowGroup = ScreenManager.Groups.BACKGROUND) {

    init {
        GUITexturePane(rootChild, "Test GUI background", 0, 0, Image.GUI.MAIN_MENU_BACKGROUND, Game.WIDTH, Game.HEIGHT).run {
            GUIButton(this, "Test GUI back button", 1, 1, "Back to Main Menu", onRelease = {
                this@TestGUI.open = false
                MainMenuGUI.open = true
            })
        }
    }
}