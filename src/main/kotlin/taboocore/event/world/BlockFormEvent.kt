package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 方块自然形成事件（如雪/冰形成、黑曜石/圆石由水与岩浆接触生成等）
 */
class BlockFormEvent {

    /**
     * 方块形成前触发
     *
     * @property level 所在世界
     * @property pos 方块位置
     * @property oldState 形成前的方块状态
     * @property newState 形成后的方块状态（可修改）
     */
    class Pre(
        val level: ServerLevel,
        val pos: BlockPos,
        val oldState: BlockState,
        var newState: BlockState
    ) : CancelableInternalEvent()

    /**
     * 方块形成后触发
     *
     * @property level 所在世界
     * @property pos 方块位置
     * @property oldState 形成前的方块状态
     * @property newState 形成后的方块状态
     */
    class Post(
        val level: ServerLevel,
        val pos: BlockPos,
        val oldState: BlockState,
        val newState: BlockState
    ) : InternalEvent()

    companion object {
        /**
         * 方块形成前触发，返回事件对象（可读取修改后的 newState），返回 null 表示事件被取消
         */
        fun firePre(level: ServerLevel, pos: BlockPos, oldState: BlockState, newState: BlockState): Pre? {
            val event = Pre(level, pos, oldState, newState)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 方块形成后触发
         */
        fun firePost(level: ServerLevel, pos: BlockPos, oldState: BlockState, newState: BlockState) {
            Post(level, pos, oldState, newState).call()
        }
    }
}
