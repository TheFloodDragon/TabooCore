package taboocore.mixin

import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.packet.PacketReceiveEvent

/**
 * 数据包接收拦截 Mixin
 *
 * 注意：[channelRead0] 运行在 Netty IO 线程而非游戏主线程。
 * 在 [PacketReceiveEvent.Pre] / [PacketReceiveEvent.Post] 的监听器中
 * 若需访问游戏状态（方块、实体、调度器等），必须手动调度回主线程。
 *
 * 数据包发送事件（[taboocore.packet.PacketSendEvent]）由
 * [MixinServerCommonPacketListenerImpl] 在游戏主线程处理。
 */
@Mixin(net.minecraft.network.Connection::class)
abstract class MixinConnection {

    @Shadow
    private var packetListener: PacketListener? = null

    @Unique
    private var receivePacketFired: Boolean = false

    // ========== PacketReceiveEvent ==========

    @Inject(
        method = ["channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"],
        at = [At("HEAD")],
        cancellable = true
    )
    private fun onChannelRead0Pre(ctx: io.netty.channel.ChannelHandlerContext, packet: Packet<*>, ci: CallbackInfo) {
        receivePacketFired = false
        val listener = packetListener
        if (listener !is ServerGamePacketListenerImpl) return
        val player = listener.player
        if (PacketReceiveEvent.firePre(player, packet)) {
            ci.cancel()
            return
        }
        receivePacketFired = true
    }

    @Inject(
        method = ["channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"],
        at = [At("RETURN")]
    )
    private fun onChannelRead0Post(ctx: io.netty.channel.ChannelHandlerContext, packet: Packet<*>, ci: CallbackInfo) {
        if (!receivePacketFired) return
        receivePacketFired = false
        val listener = packetListener
        if (listener !is ServerGamePacketListenerImpl) return
        PacketReceiveEvent.firePost(listener.player, packet)
    }
}
