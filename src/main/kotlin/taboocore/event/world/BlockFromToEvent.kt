package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 液体流动事件（液体从一个方块流向另一个方块）
 */
class BlockFromToEvent {

    /**
     * 液体流动前触发
     *
     * @property level 所在世界
     * @property fromPos 液体源位置
     * @property toPos 液体目标位置
     * @property fluid 液体方块状态（可修改）
     */
    class Pre(
        val level: ServerLevel,
        val fromPos: BlockPos,
        val toPos: BlockPos,
        var fluid: BlockState
    ) : CancelableInternalEvent()

    /**
     * 液体流动后触发
     *
     * @property level 所在世界
     * @property fromPos 液体源位置
     * @property toPos 液体目标位置
     * @property fluid 液体方块状态
     */
    class Post(
        val level: ServerLevel,
        val fromPos: BlockPos,
        val toPos: BlockPos,
        val fluid: BlockState
    ) : InternalEvent()

    companion object {
        /**
         * 液体流动前触发，返回事件对象（可读取修改后的 fluid），返回 null 表示事件被取消
         */
        fun firePre(level: ServerLevel, fromPos: BlockPos, toPos: BlockPos, fluid: BlockState): Pre? {
            val event = Pre(level, fromPos, toPos, fluid)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 液体流动后触发
         */
        fun firePost(level: ServerLevel, fromPos: BlockPos, toPos: BlockPos, fluid: BlockState) {
            Post(level, fromPos, toPos, fluid).call()
        }
    }
}
