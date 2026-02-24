package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体骑乘事件
 */
class EntityMountEvent {

    /**
     * 实体骑乘前触发
     *
     * @property entity 骑乘的实体（乘客）
     * @property mount 被骑乘的实体（载具）
     */
    class Pre(
        val entity: Entity,
        val mount: Entity
    ) : CancelableInternalEvent()

    /**
     * 实体骑乘后触发
     *
     * @property entity 骑乘的实体（乘客）
     * @property mount 被骑乘的实体（载具）
     */
    class Post(
        val entity: Entity,
        val mount: Entity
    ) : InternalEvent()

    companion object {
        /**
         * 实体骑乘前触发，返回 null 表示事件被取消
         */
        fun firePre(entity: Entity, mount: Entity): Pre? {
            val event = Pre(entity, mount)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 实体骑乘后触发
         */
        fun firePost(entity: Entity, mount: Entity) {
            Post(entity, mount).call()
        }
    }
}
