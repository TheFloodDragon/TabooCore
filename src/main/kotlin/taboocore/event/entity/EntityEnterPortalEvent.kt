package taboocore.event.entity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体进入传送门事件
 */
class EntityEnterPortalEvent {

    /**
     * 实体进入传送门前触发
     *
     * @property entity 进入传送门的实体
     * @property pos 传送门方块位置
     */
    class Pre(
        val entity: Entity,
        val pos: BlockPos
    ) : CancelableInternalEvent()

    /**
     * 实体进入传送门后触发
     *
     * @property entity 进入传送门的实体
     * @property pos 传送门方块位置
     */
    class Post(
        val entity: Entity,
        val pos: BlockPos
    ) : InternalEvent()

    companion object {
        /**
         * 实体进入传送门前触发，返回 true 表示事件被取消
         */
        fun firePre(entity: Entity, pos: BlockPos): Boolean {
            val event = Pre(entity, pos)
            event.call()
            return event.isCancelled
        }

        /**
         * 实体进入传送门后触发
         */
        fun firePost(entity: Entity, pos: BlockPos) {
            Post(entity, pos).call()
        }
    }
}
