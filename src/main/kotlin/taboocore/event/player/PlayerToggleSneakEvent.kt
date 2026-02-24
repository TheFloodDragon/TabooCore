package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家切换潜行状态事件
 */
class PlayerToggleSneakEvent {

    /**
     * 玩家切换潜行状态前触发
     *
     * @property player 切换潜行的玩家
     * @property isSneaking 是否正在潜行
     */
    class Pre(
        val player: Player,
        val isSneaking: Boolean
    ) : CancelableInternalEvent()

    /**
     * 玩家切换潜行状态后触发
     *
     * @property player 切换潜行的玩家
     * @property isSneaking 是否正在潜行
     */
    class Post(
        val player: Player,
        val isSneaking: Boolean
    ) : InternalEvent()

    companion object {
        /**
         * 玩家切换潜行状态前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer, isSneaking: Boolean): Boolean {
            val event = Pre(Player.of(player), isSneaking)
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家切换潜行状态后触发
         */
        fun firePost(player: ServerPlayer, isSneaking: Boolean) {
            Post(Player.of(player), isSneaking).call()
        }
    }
}
