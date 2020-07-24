package network

import level.*
import level.block.Block
import level.moving.MovingObject
import resource.ResourceNode
import serialization.Id
import serialization.Input
import serialization.Serializer
import java.util.*

sealed class NetworkReference<T> {
    var value: T? = null

    abstract fun resolve(): T?

    override fun toString() = value.toString()
}

class ResourceNodeReference(
        @Id(1)
        val xTile: Int,
        @Id(2)
        val yTile: Int,
        @Id(3)
        val level: Level,
        @Id(4)
        val id: UUID
) : NetworkReference<ResourceNode>() {

    constructor(node: ResourceNode) : this(node.xTile, node.yTile, node.level, node.id) {
        value = node
    }

    private constructor() : this(0, 0, LevelManager.EMPTY_LEVEL, UUID.randomUUID())

    override fun resolve(): ResourceNode? {
        val nodes = level.getResourceNodesAt(xTile, yTile)
        return nodes.firstOrNull { it.id == id }
    }
}

abstract class LevelObjectReference(@Id(1)
                                    val level: Level,
                                    @Id(2)
                                    val objectId: UUID) : NetworkReference<LevelObject>()

class GhostLevelObjectReference(val obj: GhostLevelObject) : LevelObjectReference(obj.level, obj.id) {

    init {
        value = obj
    }

    override fun resolve() = obj
}

class MovingObjectReference(level: Level, objectId: UUID,
                            @Id(3)
                            val xPixel: Int,
                            @Id(4)
                            val yPixel: Int) : LevelObjectReference(level, objectId) {

    constructor(movingObject: MovingObject) : this(movingObject.level, movingObject.id, movingObject.xPixel, movingObject.yPixel) {
        value = movingObject
    }

    private constructor() : this(LevelManager.EMPTY_LEVEL, UUID.randomUUID(), 0, 0)

    override fun resolve(): MovingObject? {
        val xChunk = xPixel shr CHUNK_PIXEL_EXP
        val yChunk = yPixel shr CHUNK_PIXEL_EXP
        var currentChunkRange = 0
        var currentChunks = setOf(level.getChunkFromPixel(xPixel, yPixel))
        while (currentChunkRange < Math.max(level.widthChunks - xChunk - 1, xChunk + 1) || currentChunkRange < Math.max(level.heightChunks - yChunk - 1, yChunk + 1)) {
            // while we still have range to go
            for (chunk in currentChunks) {
                val moving = chunk.data.moving.filter { it.id == objectId }.firstOrNull()
                if (moving != null) {
                    return moving
                }
            }
            currentChunkRange++
            if (currentChunkRange > 3) {
                println("Resolving reference taking abnormally long, possible desync")
            }
            currentChunks = level.getChunksFromChunkRectangle(xChunk - currentChunkRange, yChunk - currentChunkRange, xChunk + currentChunkRange, yChunk + currentChunkRange)
        }
        return null
    }
}

class DroppedItemReference(level: Level, objectId: UUID,
                           @Id(3)
                           val xPixel: Int,
                           @Id(4)
                           val yPixel: Int) : LevelObjectReference(level, objectId) {

    constructor(droppedItem: DroppedItem) : this(droppedItem.level, droppedItem.id, droppedItem.xPixel, droppedItem.yPixel) {
        value = droppedItem
    }

    private constructor() : this(LevelManager.EMPTY_LEVEL, UUID.randomUUID(), 0, 0)

    override fun resolve(): DroppedItem? {
        val items = level.getDroppedItemCollisionsInSquareCenteredOn(xPixel, yPixel, 2)
        return items.firstOrNull { it.id == objectId }
    }
}

class BlockReference(level: Level, objectId: UUID,
                     @Id(3)
                     val xTile: Int,
                     @Id(4)
                     val yTile: Int) : LevelObjectReference(level, objectId) {

    constructor(block: Block) : this(block.level, block.id, block.xTile, block.yTile) {
        value = block
    }

    private constructor() : this(LevelManager.EMPTY_LEVEL, UUID.randomUUID(), 0, 0)

    override fun resolve(): Block? {
        val block = level.getBlockAt(xTile, yTile)
        if (block == null) {
            println("no block at $xTile, $yTile")
            println("blocks in that chunk: ${level.getChunkFromTile(xTile, yTile).data.blocks.joinToString()}")
            return null
        }
        if (block.id != objectId) {
            println("block has wrong id")
            return null
        }
        return block
    }

    override fun toString(): String {
        return "Reference to block at $xTile, $yTile: $value"
    }
}

class NetworkReferenceSerializer : Serializer.Tagged<NetworkReference<*>>(true) {

    override fun read(newInstance: Any, input: Input) {
        super.read(newInstance, input)
        newInstance as NetworkReference<Any?>
        newInstance.value = newInstance.resolve()
    }
}