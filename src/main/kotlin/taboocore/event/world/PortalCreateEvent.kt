package taboocore.event.world

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 传送门创建事件
 */
class PortalCreateEvent {

    /**
     * 传送门创建前触发
     *
     * @property blocks 传送门方块位置列表
     * @property reason 创建原因
     * @property creator 创建者实体（可能为 null）
     */
    class Pre(
        val blocks: List<BlockPos>,
        val reason: String,
        val creator: Entity?
    ) : CancelableInternalEvent()

    /**
     * 传送门创建后触发
     *
     * @property blocks 传送门方块位置列表
     * @property reason 创建原因
     * @property creator 创建者实体（可能为 null）
     */
    class Post(
        val blocks: List<BlockPos>,
        val reason: String,
        val creator: Entity?
    ) : InternalEvent()

    companion object {
        /**
         * 传送门创建前触发，返回 true 表示事件被取消
         */
        fun firePre(blocks: List<BlockPos>, reason: String, creator: Entity?): Boolean {
            val event = Pre(blocks, reason, creator)
            event.call()
            return event.isCancelled
        }

        /**
         * 传送门创建后触发
         */
        fun firePost(blocks: List<BlockPos>, reason: String, creator: Entity?) {
            Post(blocks, reason, creator).call()
        }
    }
}
