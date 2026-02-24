package taboocore.mixin

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.PlayerAdvancements
import net.minecraft.server.level.ServerPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.player.PlayerAdvancementDoneEvent

@Mixin(PlayerAdvancements::class)
abstract class MixinPlayerAdvancements {

    @Shadow
    lateinit var player: ServerPlayer

    @Unique
    private var awardFired: Boolean = false

    @Inject(method = ["award"], at = [At("HEAD")], cancellable = true)
    private fun onAwardPre(holder: AdvancementHolder, criterion: String, cir: CallbackInfoReturnable<Boolean>) {
        awardFired = false
        val event = PlayerAdvancementDoneEvent.firePre(player, holder, criterion)
        if (event == null) {
            cir.returnValue = false
            return
        }
        awardFired = true
    }

    @Inject(method = ["award"], at = [At("RETURN")])
    private fun onAwardPost(holder: AdvancementHolder, criterion: String, cir: CallbackInfoReturnable<Boolean>) {
        if (!awardFired) return
        awardFired = false
        if (cir.returnValue == true) {
            PlayerAdvancementDoneEvent.firePost(player, holder, criterion)
        }
    }
}
