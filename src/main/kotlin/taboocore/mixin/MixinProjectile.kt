package taboocore.mixin

import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.HitResult
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.entity.ProjectileHitEvent
import taboocore.event.entity.ProjectileLaunchEvent

@Mixin(Projectile::class)
abstract class MixinProjectile {

    @Inject(method = ["onHit"], at = [At("HEAD")], cancellable = true)
    private fun onHitPre(hitResult: HitResult, ci: CallbackInfo) {
        if (hitResult.type == HitResult.Type.MISS) return
        if (ProjectileHitEvent.firePre(this as Projectile, hitResult)) {
            ci.cancel()
        }
    }

    @Inject(method = ["onHit"], at = [At("TAIL")])
    private fun onHitPost(hitResult: HitResult, ci: CallbackInfo) {
        if (hitResult.type == HitResult.Type.MISS) return
        ProjectileHitEvent.firePost(this as Projectile, hitResult)
    }

    // ============ ProjectileLaunchEvent ============

    @Inject(method = ["shoot"], at = [At("HEAD")], cancellable = true)
    private fun onShootPre(xd: Double, yd: Double, zd: Double, pow: Float, uncertainty: Float, ci: CallbackInfo) {
        if (ProjectileLaunchEvent.firePre(this as Projectile) == null) {
            ci.cancel()
        }
    }

    @Inject(method = ["shoot"], at = [At("TAIL")])
    private fun onShootPost(xd: Double, yd: Double, zd: Double, pow: Float, uncertainty: Float, ci: CallbackInfo) {
        ProjectileLaunchEvent.firePost(this as Projectile)
    }
}
