package level.entity.robot

import io.*
import item.weapon.Projectile
import item.weapon.Weapon
import item.weapon.WeaponItemType
import level.LevelObject
import level.entity.Entity
import misc.Numbers
import kotlin.math.PI
import kotlin.math.atan
import level.LevelManager
import main.Game

open class Robot(type: RobotType<out Robot>, xPixel: Int, yPixel: Int, rotation: Int = 0) : Entity(type, xPixel, yPixel, rotation), ControlPressHandler {

    override val type = type

    init {
        if(!Game.IS_SERVER) {
            InputManager.registerControlPressHandler(this, ControlPressHandlerType.LEVEL_ANY_UNDER_MOUSE, Control.SECONDARY_INTERACT)
        }
    }

    override fun onAddToLevel() {
        super.onAddToLevel()
        weapon = Weapon(WeaponItemType.MACHINE_GUN)
    }

    override fun handleControlPress(p: ControlPress) {
        if(p.pressType == PressType.PRESSED && inLevel) {
            
        }
    }

    override fun toString(): String {
        return "Robot at $xPixel, $yPixel"
    }
}