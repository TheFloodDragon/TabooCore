package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体着火事件
 */
class EntityCombustEvent {

    /**
     * 实体着火前触发
     *
     * @property entity 着火的实体
     * @property duration 着火持续时间（tick 数，可修改）
     */
    class Pre(
        val entity: Entity,
        var duration: Int
    ) : CancelableInternalEvent()

    /**
     * 实体着火后触发
     *
     * @property entity 着火的实体
     * @property duration 着火持续时间（tick 数）
     */
    class Post(
        val entity: Entity,
        val duration: Int
    ) : InternalEvent()

    companion object {
        /**
         * 实体着火前触发，返回事件对象（可能已被修改），返回 null 表示事件被取消
         */
        fun firePre(entity: Entity, duration: Int): Pre? {
            val event = Pre(entity, duration)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 实体着火后触发
         */
        fun firePost(entity: Entity, duration: Int) {
            Post(entity, duration).call()
        }
    }
}
