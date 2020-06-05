package level.block

import com.badlogic.gdx.Input
import io.PressType
import level.getChunkFromTile
import level.getTileAt
import level.tile.OreTile
import resource.give
import resource.output
import serialization.Id

class MinerBlock(xTile: Int, yTile: Int, rotation: Int) : MachineBlock(MachineBlockType.MINER, xTile, yTile, rotation) {

    @Id(23)
    val output = containers.first()

    override fun onFinishWork() {
        for (x in 0 until type.widthTiles) {
            for (y in 0 until type.heightTiles) {
                val tile = level.getTileAt(xTile + x, yTile + y)
                if (tile is OreTile) {
                    // fill up the internal inventory
                    if (output.add(tile.type.minedItem, 1)) {
                        tile.amount -= 1
                        return
                    } else {
                        currentWork = type.maxWork
                    }
                }
            }
        }
    }

    override fun onInteractOn(type: PressType, xPixel: Int, yPixel: Int, button: Int, shift: Boolean, ctrl: Boolean, alt: Boolean) {
        if(type == PressType.RELEASED && button == Input.Buttons.LEFT && !shift && !alt && !ctrl) {
            this.type.guiPool!!.toggle(this)
        }
    }
}