package level.block

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag
import audio.AudioManager
import audio.AudioManager.SoundSource

abstract class MachineBlock(override val type: MachineBlockType<out MachineBlock>, xTile: Int, yTile: Int, rotation: Int, on: Boolean = type.defaultOn) : Block(type, xTile, yTile, rotation) {

    @Tag(20)
    var on = on
        set(value) {
            if (!value && field) {
                onTurnOff()
                if(currentSound != null)
                    currentSound!!.playing = false
            } else if (value && !field) {
                onTurnOn()
                if(currentSound == null && type.onSound != null) {
                    AudioManager.play(type.onSound!!, xPixel, yPixel, true)
                }
            }
            field = value
        }

    @Tag(21)
    var currentSound: SoundSource? = null
    @Tag(22)
    var currentWork = 0

    open fun onTurnOn() {

    }

    open fun onTurnOff() {

    }

    open fun onFinishWork() {

    }

    override fun update() {
        if (on) {
            currentWork++
            onWork()
            if (currentWork >= (type.maxWork / type.speed).toInt()) {
                currentWork = 0
                if (!type.loop)
                    on = false
                onFinishWork()
            }
        }
    }

    open fun onWork() {
        // use power and type.defaultEfficiency
    }
}