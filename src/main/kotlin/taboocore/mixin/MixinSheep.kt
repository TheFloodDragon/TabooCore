package taboocore.mixin

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.animal.sheep.Sheep
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.player.PlayerShearEntityEvent

@Mixin(Sheep::class)
abstract class MixinSheep {

    @Unique
    private var shearFired: Boolean = false

    // ========== PlayerShearEntityEvent ==========

    @Inject(method = ["mobInteract"], at = [At("HEAD")], cancellable = true)
    private fun onMobInteractPre(player: Player, hand: InteractionHand, cir: CallbackInfoReturnable<InteractionResult>) {
        if (player !is ServerPlayer) return
        val item = player.getItemInHand(hand)
        if (!item.`is`(Items.SHEARS)) return
        shearFired = false
        if (PlayerShearEntityEvent.firePre(player, this as Sheep, hand, item.copy())) {
            cir.returnValue = InteractionResult.FAIL
            return
        }
        shearFired = true
    }

    @Inject(method = ["mobInteract"], at = [At("RETURN")])
    private fun onMobInteractPost(player: Player, hand: InteractionHand, cir: CallbackInfoReturnable<InteractionResult>) {
        if (!shearFired) return
        shearFired = false
        if (player !is ServerPlayer) return
        val item = player.getItemInHand(hand)
        PlayerShearEntityEvent.firePost(player, this as Sheep, hand, item)
    }
}
