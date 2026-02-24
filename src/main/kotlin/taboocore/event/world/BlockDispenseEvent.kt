package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 发射器发射物品事件
 */
class BlockDispenseEvent {

    /**
     * 发射器发射前触发
     *
     * @property level 所在世界
     * @property pos 发射器位置
     * @property block 发射器方块状态
     * @property item 被发射的物品（可修改）
     * @property direction 发射方向
     */
    class Pre(
        val level: ServerLevel,
        val pos: BlockPos,
        val block: BlockState,
        var item: ItemStack,
        val direction: Direction
    ) : CancelableInternalEvent()

    /**
     * 发射器发射后触发
     *
     * @property level 所在世界
     * @property pos 发射器位置
     * @property block 发射器方块状态
     * @property item 被发射的物品
     * @property direction 发射方向
     */
    class Post(
        val level: ServerLevel,
        val pos: BlockPos,
        val block: BlockState,
        val item: ItemStack,
        val direction: Direction
    ) : InternalEvent()

    companion object {
        /**
         * 发射器发射前触发，返回事件对象（可读取修改后的 item），返回 null 表示事件被取消
         */
        fun firePre(level: ServerLevel, pos: BlockPos, block: BlockState, item: ItemStack, direction: Direction): Pre? {
            val event = Pre(level, pos, block, item, direction)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 发射器发射后触发
         */
        fun firePost(level: ServerLevel, pos: BlockPos, block: BlockState, item: ItemStack, direction: Direction) {
            Post(level, pos, block, item, direction).call()
        }
    }
}
