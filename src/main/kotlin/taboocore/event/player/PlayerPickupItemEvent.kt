package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家拾取物品事件
 */
class PlayerPickupItemEvent {

    /**
     * 玩家拾取物品前触发
     *
     * @property player 拾取物品的玩家
     * @property itemEntity 被拾取的物品实体
     */
    class Pre(
        val player: Player,
        val itemEntity: ItemEntity
    ) : CancelableInternalEvent()

    /**
     * 玩家拾取物品后触发
     *
     * @property player 拾取物品的玩家
     * @property itemEntity 被拾取的物品实体
     */
    class Post(
        val player: Player,
        val itemEntity: ItemEntity
    ) : InternalEvent()

    companion object {
        /**
         * 玩家拾取物品前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer, itemEntity: ItemEntity): Boolean {
            val event = Pre(Player.of(player), itemEntity)
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家拾取物品后触发
         */
        fun firePost(player: ServerPlayer, itemEntity: ItemEntity) {
            Post(Player.of(player), itemEntity).call()
        }
    }
}
