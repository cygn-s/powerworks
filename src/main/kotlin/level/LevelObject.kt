package level

import graphics.Renderer
import level.block.GhostBlock
import main.DebugCode
import main.Game
import java.io.DataOutputStream

abstract class LevelObject protected constructor(
        open val type: LevelObjectType<out LevelObject>,
        open val xPixel: Int, open val yPixel: Int,
        rotation: Int = 0,
        /**
         * Should be the default (unrotated) instance of the hitbox.
         */
        hitbox: Hitbox = type.hitbox,
        requiresUpdate: Boolean = type.requiresUpdate) {

    open val xTile = xPixel shr 4
    open val yTile = yPixel shr 4
    open val xChunk = xTile shr CHUNK_TILE_EXP
    open val yChunk = yTile shr CHUNK_TILE_EXP

    var hitbox = Hitbox.rotate(hitbox, rotation)
        private set

    var rotation = rotation
        set(value) {
            if (field != value) {
                field = value
                hitbox = Hitbox.rotate(type.hitbox, value)
            }
        }

    /**
     * If this has been added to the level
     */
    open var inLevel = false
        set(value) {
            if (field && !value) {
                field = value
                onRemoveFromLevel()
            } else if (!field && value) {
                field = value
                onAddToLevel()
            }
        }

    var requiresUpdate: Boolean = requiresUpdate
        set(value) {
            val c = Level.Chunks.get(xChunk, yChunk)
            if (field && !value) {
                c.removeUpdateRequired(this)
            } else if (!field && value) {
                c.addUpdateRequired(this)
            }
        }

    init {

    }

    open fun render() {
        if (Game.currentDebugCode == DebugCode.RENDER_HITBOXES)
            renderHitbox()
    }

    /**
     * When this gets put in the level
     * Called when inLevel is changed to true, should usually be from Level.add
     */
    open fun onAddToLevel() {
    }

    /**
     * When this gets taken out of the level
     * Called when inLevel is changed to false, should usually be from Level.remove
     */
    open fun onRemoveFromLevel() {
    }

    protected fun renderHitbox() {
        Renderer.renderFilledRectangle(xPixel + hitbox.xStart, yPixel + hitbox.yStart, hitbox.width, hitbox.height, 0xFF0010)
    }

    /**
     * Only called if type.requiresUpdate is true
     */
    open fun update() {
    }

    /**
     * Called if this collides with something or something else collides with it
     */
    open fun onCollide(o: LevelObject) {
    }

    /**
     * Checks if this level object would have a collision at the given coordinates, excludes all collisions that would happen with a level object not matching the given predicate
     * @param predicate if null, all level objects are checked
     * */
    open fun getCollision(xPixel: Int, yPixel: Int, predicate: ((LevelObject) -> Boolean)? = null): LevelObject? {
        if (hitbox == Hitbox.NONE)
            return null
        return Level.getCollision(this, xPixel, yPixel, predicate)
    }

    /**
     * Should write all
     */
    open fun save(out: DataOutputStream) {
        out.writeInt(type.id)
        out.writeInt(xPixel)
        out.writeInt(yPixel)
        out.writeInt(rotation)
    }
}