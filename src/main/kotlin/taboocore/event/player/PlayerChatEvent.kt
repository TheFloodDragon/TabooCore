package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家聊天事件
 */
class PlayerChatEvent {

    /**
     * 玩家聊天前触发
     *
     * @property player 发送消息的玩家
     * @property message 聊天消息内容
     */
    class Pre(
        val player: Player,
        val message: String
    ) : CancelableInternalEvent()

    /**
     * 玩家聊天后触发
     *
     * @property player 发送消息的玩家
     * @property message 聊天消息内容
     */
    class Post(
        val player: Player,
        val message: String
    ) : InternalEvent()

    companion object {
        /**
         * 玩家聊天前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer, message: String): Boolean {
            val event = Pre(Player.of(player), message)
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家聊天后触发
         */
        fun firePost(player: ServerPlayer, message: String) {
            Post(Player.of(player), message).call()
        }
    }
}
