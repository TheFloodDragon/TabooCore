package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家执行命令事件
 */
class PlayerCommandEvent {

    /**
     * 玩家执行命令前触发
     *
     * @property player 执行命令的玩家
     * @property command 命令字符串
     */
    class Pre(
        val player: Player,
        val command: String
    ) : CancelableInternalEvent()

    /**
     * 玩家执行命令后触发
     *
     * @property player 执行命令的玩家
     * @property command 命令字符串
     */
    class Post(
        val player: Player,
        val command: String
    ) : InternalEvent()

    companion object {
        /**
         * 玩家执行命令前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer, command: String): Boolean {
            val event = Pre(Player.of(player), command)
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家执行命令后触发
         */
        fun firePost(player: ServerPlayer, command: String) {
            Post(Player.of(player), command).call()
        }
    }
}
