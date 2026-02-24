package taboocore.mixin

import net.minecraft.network.Connection
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.players.PlayerList
import net.minecraft.world.entity.Entity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.bridge.EventBridge
import taboocore.event.player.PlayerRespawnEvent

@Mixin(PlayerList::class)
abstract class MixinPlayerList {

    @Inject(method = ["placeNewPlayer"], at = [At("TAIL")])
    private fun onPlayerJoin(connection: Connection, player: ServerPlayer, cookie: CommonListenerCookie, ci: CallbackInfo) {
        EventBridge.firePlayerJoin(player)
    }

    @Inject(method = ["remove"], at = [At("HEAD")])
    private fun onPlayerQuit(player: ServerPlayer, ci: CallbackInfo) {
        EventBridge.firePlayerQuit(player)
    }

    @Inject(method = ["respawn"], at = [At("HEAD")], cancellable = true)
    private fun onRespawnPre(player: ServerPlayer, keepAllPlayerData: Boolean, removalReason: Entity.RemovalReason, cir: CallbackInfoReturnable<ServerPlayer>) {
        if (PlayerRespawnEvent.firePre(player)) {
            cir.returnValue = player
        }
    }

    @Inject(method = ["respawn"], at = [At("RETURN")])
    private fun onRespawnPost(player: ServerPlayer, keepAllPlayerData: Boolean, removalReason: Entity.RemovalReason, cir: CallbackInfoReturnable<ServerPlayer>) {
        PlayerRespawnEvent.firePost(player)
    }
}
