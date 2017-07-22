package io

import io.OutputManager as out

private interface ControlBind

private data class KeyBind(val keyCode: Int, val modifier: Int, val control: Control) : ControlBind

private data class MouseBind(val mouseButton: Int, val modifier: Int, val control: Control) : ControlBind

private data class MouseWheelBind(val wheelDirection: Int, val modifier: Int, val control: Control) : ControlBind

enum class ControlMap private constructor(path: String) {
    DEFAULT("/settings/controls/default.txt");

    private val binds: List<ControlBind> = mutableListOf()

    init {
        val text = ControlMap::class.java.getResource(path).readText()
        val lines = text.split(delimiters = "\n")
        var mode:Int = 0
        for(s in lines) {
            if(s.contains(char=':')) {
                val split = s.split(delimiters=':')
                val first = split[0]
                val second = split[1]
                println(first + ", " + second)
            } else {
                when(s) {
                    "k" -> mode = 0
                    "m" -> mode = 1
                    "mw" -> mode = 2
                }
            }
        }
    }

    fun translateKey(keyCode: Int, modifier: Int) : Set<Control> {
        val controls = mutableSetOf<Control>()
        for(b in binds) {
            if(b is KeyBind) {
                if(b.keyCode == keyCode && (b.modifier == -1 || b.modifier == modifier))
                    controls.add(b.control)
            }
        }
        return controls
    }

    fun translateMouse(mouseButton: Int, modifier: Int) : Set<Control> {
        val controls = mutableSetOf<Control>()
        for(b in binds) {
            if(b is MouseBind) {
                if(b.mouseButton == mouseButton && (b.modifier == -1 || b.modifier == modifier))
                    controls.add(b.control)
            }
        }
        return controls
    }

    fun translateMouseWheel(wheelDirection: Int, modifier: Int) : Set<Control> {
        val controls = mutableSetOf<Control>()
        for(b in binds) {
            if(b is MouseWheelBind) {
                if(b.wheelDirection == wheelDirection && (b.modifier == -1 || b.modifier == modifier))
                    controls.add(b.control)
            }
        }
        return controls
    }
}