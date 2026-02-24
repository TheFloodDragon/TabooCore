package taboocore.event.entity

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体选择目标事件
 */
class EntityTargetEvent {

    /**
     * 实体选择目标前触发
     *
     * @property entity 选择目标的生物
     * @property target 被选择的目标实体（可能为 null 表示清除目标）
     */
    class Pre(
        val entity: Mob,
        val target: LivingEntity?
    ) : CancelableInternalEvent()

    /**
     * 实体选择目标后触发
     *
     * @property entity 选择目标的生物
     * @property target 被选择的目标实体（可能为 null 表示清除目标）
     */
    class Post(
        val entity: Mob,
        val target: LivingEntity?
    ) : InternalEvent()

    companion object {
        /**
         * 实体选择目标前触发，返回 true 表示事件被取消
         */
        fun firePre(mob: Mob, target: LivingEntity?): Boolean {
            val event = Pre(mob, target)
            event.call()
            return event.isCancelled
        }

        /**
         * 实体选择目标后触发
         */
        fun firePost(mob: Mob, target: LivingEntity?) {
            Post(mob, target).call()
        }
    }
}
