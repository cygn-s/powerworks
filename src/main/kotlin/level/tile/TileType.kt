package level.tile

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import graphics.ImageCollection
import item.ItemType
import item.OreItemType

private var nextID = 0

open class OreTileType(textures: ImageCollection = ImageCollection.GRASS_COPPER_ORE_TILE, name: String = "Error",
                       val maxAmount: Int = 1000,
                       val minAmount: Int = 1000,
                       val minedItem: ItemType = ItemType.ERROR,
                       val backgroundType: TileType = GRASS,
                       val scatter: Int = 1,
                       val generationChance: Double = .1) : TileType(name, textures) {

    init {
        ALL.add(this)
    }

    companion object {
        val ALL = mutableListOf<OreTileType>()

        val GRASS_IRON_ORE = OreTileType(ImageCollection.GRASS_IRON_ORE_TILE, "Grass and iron ore",
                9000,
                3000,
                OreItemType.IRON_ORE,
                TileType.GRASS,
                5,
                .1)

        val GRASS_COPPER_ORE = OreTileType(ImageCollection.GRASS_COPPER_ORE_TILE, "Grass and copper ore",
                5000,
                3000,
                OreItemType.COPPER_ORE,
                TileType.GRASS,
                3,
                .07)
    }
}

open class TileType(val name: String, val textures: Array<TextureRegion>) {

    constructor(name: String, textures: ImageCollection) : this(name, textures.textures)
    constructor(name: String, texture: TextureRegion) : this(name, arrayOf(texture))

    val id = nextID++

    init {
        ALL.add(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other is TileType) {
            if (other.id == this.id)
                return true
        }
        return false
    }

    override fun toString() = name

    override fun hashCode(): Int {
        return id
    }

    companion object {
        val ALL = mutableListOf<TileType>()

        val GRASS = TileType("Grass", ImageCollection.GRASS_TILE)
    }
}

class TileTypeSerializer : Serializer<TileType>() {
    override fun write(kryo: Kryo, output: Output, `object`: TileType) {
        output.writeInt(`object`.id)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out TileType>): TileType {
        val id = input.readInt()
        return TileType.ALL.first { it.id == id }.apply { kryo.reference(this) }
    }

}