package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体下坐骑事件
 */
class EntityDismountEvent {

    /**
     * 实体下坐骑前触发
     *
     * @property entity 下坐骑的实体（乘客）
     * @property dismounted 被离开的实体（载具）
     */
    class Pre(
        val entity: Entity,
        val dismounted: Entity
    ) : CancelableInternalEvent()

    /**
     * 实体下坐骑后触发
     *
     * @property entity 下坐骑的实体（乘客）
     * @property dismounted 被离开的实体（载具）
     */
    class Post(
        val entity: Entity,
        val dismounted: Entity
    ) : InternalEvent()

    companion object {
        /**
         * 实体下坐骑前触发，返回 null 表示事件被取消
         */
        fun firePre(entity: Entity, dismounted: Entity): Pre? {
            val event = Pre(entity, dismounted)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 实体下坐骑后触发
         */
        fun firePost(entity: Entity, dismounted: Entity) {
            Post(entity, dismounted).call()
        }
    }
}
