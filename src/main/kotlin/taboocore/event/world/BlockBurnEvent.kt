package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 方块燃烧事件
 */
class BlockBurnEvent {

    /**
     * 方块被火焰烧毁前触发
     *
     * @property block 方块状态
     * @property x 方块 X 坐标
     * @property y 方块 Y 坐标
     * @property z 方块 Z 坐标
     */
    class Pre(
        val block: BlockState,
        val x: Int,
        val y: Int,
        val z: Int
    ) : CancelableInternalEvent()

    /**
     * 方块被火焰烧毁后触发
     *
     * @property block 方块状态
     * @property x 方块 X 坐标
     * @property y 方块 Y 坐标
     * @property z 方块 Z 坐标
     */
    class Post(
        val block: BlockState,
        val x: Int,
        val y: Int,
        val z: Int
    ) : InternalEvent()

    companion object {
        /**
         * 方块被火焰烧毁前触发，返回 true 表示事件被取消
         */
        fun fireBlockBurnPre(level: ServerLevel, pos: BlockPos, state: BlockState): Boolean {
            val event = Pre(state, pos.x, pos.y, pos.z)
            event.call()
            return event.isCancelled
        }

        /**
         * 方块被火焰烧毁后触发
         */
        fun fireBlockBurnPost(level: ServerLevel, pos: BlockPos, state: BlockState) {
            Post(state, pos.x, pos.y, pos.z).call()
        }
    }
}
