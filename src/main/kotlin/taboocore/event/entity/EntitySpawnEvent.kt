package taboocore.event.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体生成事件
 */
class EntitySpawnEvent {

    /**
     * 实体生成前触发
     *
     * @property entity 生成的实体
     * @property entityType 实体类型
     * @property x 生成位置 X 坐标
     * @property y 生成位置 Y 坐标
     * @property z 生成位置 Z 坐标
     */
    class Pre(
        val entity: Entity,
        val entityType: EntityType<*>,
        val x: Double,
        val y: Double,
        val z: Double
    ) : CancelableInternalEvent()

    /**
     * 实体生成后触发
     *
     * @property entity 生成的实体
     * @property entityType 实体类型
     * @property x 生成位置 X 坐标
     * @property y 生成位置 Y 坐标
     * @property z 生成位置 Z 坐标
     */
    class Post(
        val entity: Entity,
        val entityType: EntityType<*>,
        val x: Double,
        val y: Double,
        val z: Double
    ) : InternalEvent()

    companion object {
        /**
         * 实体生成前触发，返回 true 表示事件被取消
         */
        fun firePre(entity: Entity): Boolean {
            val event = Pre(entity, entity.type, entity.x, entity.y, entity.z)
            event.call()
            return event.isCancelled
        }

        /**
         * 实体生成后触发
         */
        fun firePost(entity: Entity) {
            Post(entity, entity.type, entity.x, entity.y, entity.z).call()
        }
    }
}
