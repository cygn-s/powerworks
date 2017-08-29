package main

import level.SimplexLevel
import screen.IngameDefaultGUI
import screen.MainMenuGUI
import java.util.Random


class State(val activate: (State) -> (Unit), val deactivate: (State) -> (Unit)) {
    companion object {

        private var NEXT_STATE: State? = null

        val MAIN_MENU = State({
            MainMenuGUI.open = true
        }, {
            MainMenuGUI.open = false
        })

        val INGAME = State({
            val seed = Random().nextInt(4096).toLong()
            Game.currentLevel = SimplexLevel(256, 256, seed)
            IngameDefaultGUI.open = true
        }, {

        })

        var CURRENT_STATE = MAIN_MENU
            private set
        fun setState(s: State) {
            NEXT_STATE = s
        }

        fun update() {
            if (NEXT_STATE != null) {
                CURRENT_STATE.deactivate(NEXT_STATE!!)
                NEXT_STATE!!.activate(CURRENT_STATE)
                CURRENT_STATE = NEXT_STATE!!
                NEXT_STATE = null
            }
        }
    }
}