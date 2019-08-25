package level.entity

import level.moving.MovingObjectType

open class EntityType<T : Entity>(initializer: () -> Unit = {}) : MovingObjectType<T>() {
    var maxHealth = 100
    var moveSpeed = 1

    init {
        requiresUpdate = true
        requiresRender = true
        initializer()
    }

    companion object {
        val ERROR = EntityType<Entity>()
    }
}