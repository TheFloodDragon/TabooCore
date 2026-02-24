package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 树叶自然凋落事件
 */
class LeavesDecayEvent {

    /**
     * 树叶凋落前触发
     *
     * @property level 所在世界
     * @property pos 树叶位置
     * @property block 树叶方块状态
     */
    class Pre(
        val level: ServerLevel,
        val pos: BlockPos,
        val block: BlockState
    ) : CancelableInternalEvent()

    /**
     * 树叶凋落后触发
     *
     * @property level 所在世界
     * @property pos 树叶位置
     * @property block 树叶方块状态
     */
    class Post(
        val level: ServerLevel,
        val pos: BlockPos,
        val block: BlockState
    ) : InternalEvent()

    companion object {
        /**
         * 树叶凋落前触发，返回 null 表示事件被取消
         */
        fun firePre(level: ServerLevel, pos: BlockPos, block: BlockState): Pre? {
            val event = Pre(level, pos, block)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 树叶凋落后触发
         */
        fun firePost(level: ServerLevel, pos: BlockPos, block: BlockState) {
            Post(level, pos, block).call()
        }
    }
}
