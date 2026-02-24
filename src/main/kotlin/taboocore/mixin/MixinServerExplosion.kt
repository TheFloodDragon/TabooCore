package taboocore.mixin

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ServerExplosion
import net.minecraft.world.phys.Vec3
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.entity.EntityExplodeEvent
import taboocore.event.world.BlockExplodeEvent

@Mixin(ServerExplosion::class)
abstract class MixinServerExplosion {

    @Shadow
    abstract fun center(): Vec3

    @Shadow
    abstract fun radius(): Float

    @Shadow
    abstract fun getDirectSourceEntity(): Entity?

    @Inject(method = ["explode"], at = [At("HEAD")], cancellable = true)
    private fun onExplodePre(cir: CallbackInfoReturnable<Int>) {
        val source = getDirectSourceEntity()
        if (EntityExplodeEvent.firePre(source, center().x, center().y, center().z, radius())) {
            cir.returnValue = 0
            return
        }
        if (BlockExplodeEvent.fireBlockExplodePre(center(), radius())) {
            cir.returnValue = 0
        }
    }

    @Inject(method = ["explode"], at = [At("RETURN")])
    private fun onExplodePost(cir: CallbackInfoReturnable<Int>) {
        val source = getDirectSourceEntity()
        EntityExplodeEvent.firePost(source, center().x, center().y, center().z, radius())
        BlockExplodeEvent.fireBlockExplodePost(center(), radius(), cir.returnValue)
    }
}
