package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家重生事件
 */
class PlayerRespawnEvent {

    /**
     * 玩家重生前触发
     *
     * @property player 重生的玩家
     */
    class Pre(
        val player: Player
    ) : CancelableInternalEvent()

    /**
     * 玩家重生后触发
     *
     * @property player 重生的玩家
     */
    class Post(
        val player: Player
    ) : InternalEvent()

    companion object {
        /**
         * 玩家重生前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer): Boolean {
            val event = Pre(Player.of(player))
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家重生后触发
         */
        fun firePost(player: ServerPlayer) {
            Post(Player.of(player)).call()
        }
    }
}
