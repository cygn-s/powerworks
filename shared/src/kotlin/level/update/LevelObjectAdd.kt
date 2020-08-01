package level.update

import level.*
import level.block.Block
import level.block.BlockType
import level.block.DefaultBlock
import level.entity.robot.BrainRobot
import level.moving.MovingObject
import player.Player
import serialization.Id

class LevelObjectAdd(
        @Id(2)
        val obj: LevelObject
) : LevelUpdate(LevelModificationType.ADD_OBJECT) {

    private constructor() : this(DefaultBlock(BlockType.ERROR, 0, 0, 0))

    override val playersToSendTo: Set<Player>?
        get() = null

    private var ghostLevelObject: GhostLevelObject? = null

    override fun canAct(level: Level) = obj.hitbox == Hitbox.NONE || obj is GhostLevelObject || level.getCollisionsWith(obj.hitbox, obj.xPixel, obj.yPixel).filter { it !is GhostLevelObject }.none()

    override fun act(level: Level) {
        if (obj.level != level && obj.inLevel) { // if already in another level
            obj.level.remove(obj)
        }
        if (obj !is GhostLevelObject) {
            val collidingGhosts = level.data.ghostObjects.getCollisionsWith(obj.hitbox, obj.xPixel, obj.yPixel).toList()
            collidingGhosts.forEach { level.remove(it) }
        }
        if (obj is Block) {
            for (x in 0 until obj.type.widthTiles) {
                for (y in 0 until obj.type.heightTiles) {
                    level.getChunkFromTile(obj.xTile + x, obj.yTile + y).setBlock(obj, obj.xTile + x, obj.yTile + y, (x == 0 && y == 0))
                }
            }
            obj.level = level
            obj.inLevel = true
        } else if (obj is MovingObject) {
            if (obj.hitbox != Hitbox.NONE) {
                obj.intersectingChunks.forEach { it.data.movingOnBoundary.add(obj) }
            }
            if (obj is BrainRobot) {
                level.data.brainRobots.add(obj)
            }
            obj.level = level
            obj.inLevel = true
        } else if (obj is DroppedItem) {
            level.getChunkAt(obj.xChunk, obj.yChunk).addDroppedItem(obj)
            obj.level = level
            obj.inLevel = true
        } else if (obj is GhostLevelObject) {
            level.data.ghostObjects.add(obj)
            level.data.ghostObjects.sortWith(Comparator { o1, o2 -> o1.yPixel.compareTo(o2.yPixel) })
            obj.level = level
            obj.inLevel = true
        }
    }

    override fun actGhost(level: Level) {
        if (obj is GhostLevelObject) {
            act(level)
            return
        }
        ghostLevelObject = GhostLevelObject(obj.type, obj.xPixel, obj.yPixel, obj.rotation)
        level.add(ghostLevelObject!!)
    }

    override fun cancelActGhost(level: Level) {
        if (ghostLevelObject != null) {
            level.remove(ghostLevelObject!!)
        }
    }

    override fun equivalent(other: LevelUpdate): Boolean {
        if (other !is LevelObjectAdd) {
            return false
        }

        if (other.obj == this.obj) {
            return true
        }

        return false
    }
}