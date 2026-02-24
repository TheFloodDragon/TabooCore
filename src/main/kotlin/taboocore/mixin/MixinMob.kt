package taboocore.mixin

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.entity.EntityTargetEvent

@Mixin(Mob::class)
abstract class MixinMob {

    @Inject(method = ["setTarget"], at = [At("HEAD")], cancellable = true)
    private fun onSetTargetPre(target: LivingEntity?, ci: CallbackInfo) {
        if (EntityTargetEvent.firePre(this as Mob, target)) {
            ci.cancel()
        }
    }

    @Inject(method = ["setTarget"], at = [At("TAIL")])
    private fun onSetTargetPost(target: LivingEntity?, ci: CallbackInfo) {
        EntityTargetEvent.firePost(this as Mob, target)
    }
}
