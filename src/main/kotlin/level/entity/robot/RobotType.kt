package level.entity.robot

import graphics.ImageCollection
import level.Hitbox
import level.LevelObjectTextures
import level.entity.EntityType
import network.User
import java.util.*

class RobotType<T : Robot>(initializer: RobotType<T>.() -> Unit) : EntityType<T>() {

    init {
        initializer()
    }

    companion object {
        val STANDARD = RobotType<Robot> {
            instantiate = { xPixel, yPixel, rotation -> Robot(this, xPixel, yPixel, rotation) }
            textures = LevelObjectTextures(ImageCollection.ROBOT)
            hitbox = Hitbox.STANDARD_ROBOT
        }

        val BRAIN = RobotType<BrainRobot> {
            instantiate = { xPixel, yPixel, rotation -> BrainRobot(xPixel, yPixel, rotation, User(UUID.randomUUID(), "")) }
            textures = LevelObjectTextures(ImageCollection.ROBOT)
            hitbox = Hitbox.STANDARD_ROBOT
        }
    }
}