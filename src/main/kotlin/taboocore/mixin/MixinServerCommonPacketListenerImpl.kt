package taboocore.mixin

import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.player.PlayerKickEvent
import taboocore.packet.PacketSendEvent

@Mixin(ServerCommonPacketListenerImpl::class)
abstract class MixinServerCommonPacketListenerImpl {

    // ========== PlayerKickEvent ==========

    @Inject(method = ["disconnect(Lnet/minecraft/network/DisconnectionDetails;)V"], at = [At("HEAD")], cancellable = true)
    private fun onDisconnectPre(details: DisconnectionDetails, ci: CallbackInfo) {
        val self = this
        if (self !is ServerGamePacketListenerImpl) return
        val player: ServerPlayer = self.player
        val event = PlayerKickEvent.firePre(player, details.reason())
        if (event == null) {
            ci.cancel()
        }
    }

    @Inject(method = ["disconnect(Lnet/minecraft/network/DisconnectionDetails;)V"], at = [At("RETURN")])
    private fun onDisconnectPost(details: DisconnectionDetails, ci: CallbackInfo) {
        val self = this
        if (self !is ServerGamePacketListenerImpl) return
        val player: ServerPlayer = self.player
        PlayerKickEvent.firePost(player, details.reason())
    }

    // ========== PacketSendEvent ==========

    @Unique
    private var sendPacketFired: Boolean = false

    @Inject(method = ["send(Lnet/minecraft/network/protocol/Packet;)V"], at = [At("HEAD")], cancellable = true)
    private fun onSendPacketPre(packet: Packet<*>, ci: CallbackInfo) {
        sendPacketFired = false
        val self = this
        if (self !is ServerGamePacketListenerImpl) return
        val player: ServerPlayer = self.player
        if (PacketSendEvent.firePre(player, packet)) {
            ci.cancel()
            return
        }
        sendPacketFired = true
    }

    @Inject(method = ["send(Lnet/minecraft/network/protocol/Packet;)V"], at = [At("RETURN")])
    private fun onSendPacketPost(packet: Packet<*>, ci: CallbackInfo) {
        if (!sendPacketFired) return
        sendPacketFired = false
        val self = this
        if (self !is ServerGamePacketListenerImpl) return
        val player: ServerPlayer = self.player
        PacketSendEvent.firePost(player, packet)
    }
}
