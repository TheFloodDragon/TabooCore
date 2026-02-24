package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体传送事件
 */
class EntityTeleportEvent {

    /**
     * 实体传送前触发
     *
     * @property entity 传送的实体
     * @property fromX 传送前 X 坐标
     * @property fromY 传送前 Y 坐标
     * @property fromZ 传送前 Z 坐标
     * @property toX 传送目标 X 坐标（可修改）
     * @property toY 传送目标 Y 坐标（可修改）
     * @property toZ 传送目标 Z 坐标（可修改）
     */
    class Pre(
        val entity: Entity,
        val fromX: Double,
        val fromY: Double,
        val fromZ: Double,
        var toX: Double,
        var toY: Double,
        var toZ: Double
    ) : CancelableInternalEvent()

    /**
     * 实体传送后触发
     *
     * @property entity 传送的实体
     * @property fromX 传送前 X 坐标
     * @property fromY 传送前 Y 坐标
     * @property fromZ 传送前 Z 坐标
     * @property toX 传送目标 X 坐标
     * @property toY 传送目标 Y 坐标
     * @property toZ 传送目标 Z 坐标
     */
    class Post(
        val entity: Entity,
        val fromX: Double,
        val fromY: Double,
        val fromZ: Double,
        val toX: Double,
        val toY: Double,
        val toZ: Double
    ) : InternalEvent()

    companion object {
        /**
         * 实体传送前触发，返回事件对象（可能已被修改），返回 null 表示事件被取消
         */
        fun firePre(entity: Entity, fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Pre? {
            val event = Pre(entity, fromX, fromY, fromZ, toX, toY, toZ)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 实体传送后触发
         */
        fun firePost(entity: Entity, fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double) {
            Post(entity, fromX, fromY, fromZ, toX, toY, toZ).call()
        }
    }
}
