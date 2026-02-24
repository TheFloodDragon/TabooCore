package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体氧气值变更事件
 */
class EntityAirChangeEvent {

    /**
     * 实体氧气值变更前触发
     *
     * @property entity 氧气值变更的实体
     * @property amount 新的氧气值（可修改）
     */
    class Pre(
        val entity: Entity,
        var amount: Int
    ) : CancelableInternalEvent()

    /**
     * 实体氧气值变更后触发
     *
     * @property entity 氧气值变更的实体
     * @property amount 新的氧气值
     */
    class Post(
        val entity: Entity,
        val amount: Int
    ) : InternalEvent()

    companion object {
        /**
         * 实体氧气值变更前触发，返回事件对象（可能已被修改），返回 null 表示事件被取消
         */
        fun firePre(entity: Entity, amount: Int): Pre? {
            val event = Pre(entity, amount)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 实体氧气值变更后触发
         */
        fun firePost(entity: Entity, amount: Int) {
            Post(entity, amount).call()
        }
    }
}
