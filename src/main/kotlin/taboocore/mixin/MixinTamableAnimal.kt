package taboocore.mixin

import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.entity.EntityTameEvent

@Mixin(TamableAnimal::class)
abstract class MixinTamableAnimal {

    @Inject(method = ["tame"], at = [At("HEAD")], cancellable = true)
    private fun onTamePre(player: Player, ci: CallbackInfo) {
        if (EntityTameEvent.firePre(this as TamableAnimal, player) == null) {
            ci.cancel()
        }
    }

    @Inject(method = ["tame"], at = [At("TAIL")])
    private fun onTamePost(player: Player, ci: CallbackInfo) {
        EntityTameEvent.firePost(this as TamableAnimal, player)
    }
}
