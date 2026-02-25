package taboocore.mixin

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.player.PlayerFishEvent

@Mixin(FishingHook::class)
abstract class MixinFishingHook {

    @Shadow
    abstract fun getPlayerOwner(): net.minecraft.world.entity.player.Player?

    @Shadow
    private var hookedIn: net.minecraft.world.entity.Entity? = null

    @Shadow
    private var nibble: Int = 0

    @Unique
    private var fishFired: Boolean = false

    // ========== PlayerFishEvent ==========

    @Inject(method = ["retrieve"], at = [At("HEAD")], cancellable = true)
    private fun onRetrievePre(rod: ItemStack, cir: CallbackInfoReturnable<Int>) {
        val owner = getPlayerOwner() ?: return
        if (owner !is ServerPlayer) return

        val state = when {
            hookedIn != null -> PlayerFishEvent.State.CAUGHT_ENTITY
            nibble > 0 -> PlayerFishEvent.State.CAUGHT_FISH
            (this as FishingHook).onGround() -> PlayerFishEvent.State.IN_GROUND
            else -> PlayerFishEvent.State.FAILED_ATTEMPT
        }

        fishFired = false
        if (PlayerFishEvent.firePre(owner, this as FishingHook, state)) {
            cir.returnValue = 0
            return
        }
        fishFired = true
    }

    @Inject(method = ["retrieve"], at = [At("RETURN")])
    private fun onRetrievePost(rod: ItemStack, cir: CallbackInfoReturnable<Int>) {
        if (!fishFired) return
        fishFired = false
        val owner = getPlayerOwner() ?: return
        if (owner !is ServerPlayer) return

        val state = when (cir.returnValue) {
            3 -> PlayerFishEvent.State.CAUGHT_ENTITY
            1 -> PlayerFishEvent.State.CAUGHT_FISH
            2 -> PlayerFishEvent.State.IN_GROUND
            5 -> PlayerFishEvent.State.CAUGHT_ENTITY
            else -> PlayerFishEvent.State.FAILED_ATTEMPT
        }
        PlayerFishEvent.firePost(owner, this as FishingHook, state)
    }
}
