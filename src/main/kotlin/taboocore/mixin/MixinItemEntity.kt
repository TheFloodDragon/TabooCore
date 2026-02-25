package taboocore.mixin

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.player.PlayerPickupItemEvent

@Mixin(ItemEntity::class)
abstract class MixinItemEntity {

    @Unique
    private var pickupFired: Boolean = false

    @Unique
    private var pickupPlayer: ServerPlayer? = null

    // ========== PlayerPickupItemEvent ==========

    @Inject(method = ["playerTouch"], at = [At("HEAD")], cancellable = true)
    private fun onPlayerTouchPre(player: Player, ci: CallbackInfo) {
        if (player !is ServerPlayer) return
        pickupFired = false
        pickupPlayer = player
        if (PlayerPickupItemEvent.firePre(player, this as ItemEntity)) {
            pickupPlayer = null
            ci.cancel()
            return
        }
        pickupFired = true
    }

    @Inject(method = ["playerTouch"], at = [At("RETURN")])
    private fun onPlayerTouchPost(player: Player, ci: CallbackInfo) {
        if (!pickupFired) return
        pickupFired = false
        val p = pickupPlayer ?: return
        pickupPlayer = null
        PlayerPickupItemEvent.firePost(p, this as ItemEntity)
    }
}
