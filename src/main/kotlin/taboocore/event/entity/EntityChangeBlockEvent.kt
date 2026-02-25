package taboocore.event.entity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体改变方块事件（如下落方块着地）
 */
class EntityChangeBlockEvent {

    /**
     * 实体改变方块前触发
     *
     * @property entity 改变方块的实体
     * @property pos 方块位置
     * @property oldState 原来的方块状态
     * @property newState 新的方块状态
     */
    class Pre(
        val entity: Entity,
        val pos: BlockPos,
        val oldState: BlockState,
        val newState: BlockState
    ) : CancelableInternalEvent()

    /**
     * 实体改变方块后触发
     *
     * @property entity 改变方块的实体
     * @property pos 方块位置
     * @property oldState 原来的方块状态
     * @property newState 新的方块状态
     */
    class Post(
        val entity: Entity,
        val pos: BlockPos,
        val oldState: BlockState,
        val newState: BlockState
    ) : InternalEvent()

    companion object {
        /**
         * 实体改变方块前触发，返回 true 表示事件被取消
         */
        fun firePre(entity: Entity, pos: BlockPos, oldState: BlockState, newState: BlockState): Boolean {
            val event = Pre(entity, pos, oldState, newState)
            event.call()
            return event.isCancelled
        }

        /**
         * 实体改变方块后触发
         */
        fun firePost(entity: Entity, pos: BlockPos, oldState: BlockState, newState: BlockState) {
            Post(entity, pos, oldState, newState).call()
        }
    }
}
