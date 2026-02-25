package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 活塞收回事件
 */
class BlockPistonRetractEvent {

    /**
     * 活塞收回前触发
     *
     * @property pistonPos 活塞方块位置
     * @property blocks 被拉动的方块位置列表
     * @property direction 活塞收回方向
     */
    class Pre(
        val pistonPos: BlockPos,
        val blocks: List<BlockPos>,
        val direction: Direction
    ) : CancelableInternalEvent()

    /**
     * 活塞收回后触发
     *
     * @property pistonPos 活塞方块位置
     * @property blocks 被拉动的方块位置列表
     * @property direction 活塞收回方向
     */
    class Post(
        val pistonPos: BlockPos,
        val blocks: List<BlockPos>,
        val direction: Direction
    ) : InternalEvent()

    companion object {
        /**
         * 活塞收回前触发，返回 true 表示事件被取消
         */
        fun firePre(pistonPos: BlockPos, blocks: List<BlockPos>, direction: Direction): Boolean {
            val event = Pre(pistonPos, blocks, direction)
            event.call()
            return event.isCancelled
        }

        /**
         * 活塞收回后触发
         */
        fun firePost(pistonPos: BlockPos, blocks: List<BlockPos>, direction: Direction) {
            Post(pistonPos, blocks, direction).call()
        }
    }
}
