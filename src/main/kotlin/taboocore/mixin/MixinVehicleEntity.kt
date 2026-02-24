package taboocore.mixin

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.vehicle.VehicleEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.vehicle.VehicleDamageEvent

@Mixin(VehicleEntity::class)
abstract class MixinVehicleEntity {

    // ========== Vehicle Damage ==========

    @Unique
    private var cachedDamageSource: DamageSource? = null

    @Unique
    private var cachedDamage: Float = 0f

    @Inject(method = ["hurtServer"], at = [At("HEAD")], cancellable = true)
    private fun onHurtServerPre(level: ServerLevel, source: DamageSource, damage: Float, cir: CallbackInfoReturnable<Boolean>) {
        val event = VehicleDamageEvent.firePre(this as VehicleEntity, level, source, damage)
        if (event == null) {
            cir.returnValue = false
            return
        }
        cachedDamageSource = source
        cachedDamage = event.damage
    }

    @Inject(method = ["hurtServer"], at = [At("RETURN")])
    private fun onHurtServerPost(level: ServerLevel, source: DamageSource, damage: Float, cir: CallbackInfoReturnable<Boolean>) {
        val src = cachedDamageSource ?: return
        cachedDamageSource = null
        if (cir.returnValue == true) {
            VehicleDamageEvent.firePost(this as VehicleEntity, level, src, cachedDamage)
        }
    }
}
