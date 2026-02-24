package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 方块点燃事件
 */
class BlockIgniteEvent {

    /**
     * 方块点燃前触发
     *
     * @property block 方块状态
     * @property x 方块 X 坐标
     * @property y 方块 Y 坐标
     * @property z 方块 Z 坐标
     * @property cause 点燃原因（如 "SPREAD"）
     */
    class Pre(
        val block: BlockState,
        val x: Int,
        val y: Int,
        val z: Int,
        val cause: String
    ) : CancelableInternalEvent()

    /**
     * 方块点燃后触发
     *
     * @property block 方块状态
     * @property x 方块 X 坐标
     * @property y 方块 Y 坐标
     * @property z 方块 Z 坐标
     * @property cause 点燃原因（如 "SPREAD"）
     */
    class Post(
        val block: BlockState,
        val x: Int,
        val y: Int,
        val z: Int,
        val cause: String
    ) : InternalEvent()

    companion object {
        /**
         * 方块点燃前触发，返回 true 表示事件被取消
         */
        fun fireBlockIgnitePre(level: ServerLevel, pos: BlockPos, cause: String): Boolean {
            val state = level.getBlockState(pos)
            val event = Pre(state, pos.x, pos.y, pos.z, cause)
            event.call()
            return event.isCancelled
        }

        /**
         * 方块点燃后触发
         */
        fun fireBlockIgnitePost(level: ServerLevel, pos: BlockPos, cause: String) {
            val state = level.getBlockState(pos)
            Post(state, pos.x, pos.y, pos.z, cause).call()
        }
    }
}
