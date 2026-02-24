package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家获得经验值事件
 */
class PlayerExpChangeEvent {

    /**
     * 玩家获得经验值前触发
     *
     * @property player 获得经验值的玩家
     * @property amount 经验值变化量（可被修改）
     */
    class Pre(
        val player: Player,
        var amount: Int
    ) : CancelableInternalEvent()

    /**
     * 玩家获得经验值后触发
     *
     * @property player 获得经验值的玩家
     * @property amount 经验值变化量
     */
    class Post(
        val player: Player,
        val amount: Int
    ) : InternalEvent()

    companion object {
        /**
         * 玩家获得经验值前触发，返回事件对象，null 表示事件被取消
         */
        fun firePre(player: ServerPlayer, amount: Int): Pre? {
            val event = Pre(Player.of(player), amount)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 玩家获得经验值后触发
         */
        fun firePost(player: ServerPlayer, amount: Int) {
            Post(Player.of(player), amount).call()
        }
    }
}
