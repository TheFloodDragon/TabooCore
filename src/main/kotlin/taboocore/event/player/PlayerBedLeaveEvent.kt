package taboocore.event.player

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家离开床事件
 */
class PlayerBedLeaveEvent {

    /**
     * 玩家离开床前触发
     *
     * @property player 离开床的玩家
     * @property bed 床的方块状态
     * @property bedPos 床的位置
     */
    class Pre(
        val player: Player,
        val bed: BlockState,
        val bedPos: BlockPos
    ) : CancelableInternalEvent()

    /**
     * 玩家离开床后触发
     *
     * @property player 离开床的玩家
     * @property bed 床的方块状态
     * @property bedPos 床的位置
     */
    class Post(
        val player: Player,
        val bed: BlockState,
        val bedPos: BlockPos
    ) : InternalEvent()

    companion object {
        /**
         * 玩家离开床前触发，返回事件对象，null 表示事件被取消
         */
        fun firePre(player: ServerPlayer, bed: BlockState, bedPos: BlockPos): Pre? {
            val event = Pre(Player.of(player), bed, bedPos)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 玩家离开床后触发
         */
        fun firePost(player: ServerPlayer, bed: BlockState, bedPos: BlockPos) {
            Post(Player.of(player), bed, bedPos).call()
        }
    }
}
