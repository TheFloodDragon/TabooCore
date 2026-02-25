package taboocore.event.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import taboocore.player.Player
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 玩家挥手动画事件
 */
class PlayerAnimationEvent {

    /**
     * 动画类型枚举
     */
    enum class AnimationType {
        /** 挥臂动画 */
        ARM_SWING
    }

    /**
     * 玩家挥手动画前触发
     *
     * @property player 执行动画的玩家
     * @property animationType 动画类型
     * @property hand 使用的手
     */
    class Pre(
        val player: Player,
        val animationType: AnimationType,
        val hand: InteractionHand
    ) : CancelableInternalEvent()

    /**
     * 玩家挥手动画后触发
     *
     * @property player 执行动画的玩家
     * @property animationType 动画类型
     * @property hand 使用的手
     */
    class Post(
        val player: Player,
        val animationType: AnimationType,
        val hand: InteractionHand
    ) : InternalEvent()

    companion object {
        /**
         * 玩家挥手动画前触发，返回 true 表示事件被取消
         */
        fun firePre(player: ServerPlayer, hand: InteractionHand): Boolean {
            val event = Pre(Player.of(player), AnimationType.ARM_SWING, hand)
            event.call()
            return event.isCancelled
        }

        /**
         * 玩家挥手动画后触发
         */
        fun firePost(player: ServerPlayer, hand: InteractionHand) {
            Post(Player.of(player), AnimationType.ARM_SWING, hand).call()
        }
    }
}
