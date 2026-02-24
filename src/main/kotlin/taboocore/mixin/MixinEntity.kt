package taboocore.mixin

import net.minecraft.world.entity.Entity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.entity.EntityAirChangeEvent
import taboocore.event.entity.EntityCombustEvent
import taboocore.event.entity.EntityDismountEvent
import taboocore.event.entity.EntityMountEvent
import taboocore.event.entity.EntityTeleportEvent
import taboocore.event.vehicle.VehicleEnterEvent
import taboocore.event.vehicle.VehicleExitEvent

@Mixin(Entity::class)
abstract class MixinEntity {

    @Shadow
    abstract fun getX(): Double

    @Shadow
    abstract fun getY(): Double

    @Shadow
    abstract fun getZ(): Double

    @Shadow
    fun getVehicle(): Entity? = null

    @Shadow
    abstract fun setRemainingFireTicks(ticks: Int)

    @Shadow
    abstract fun getRemainingFireTicks(): Int

    @Shadow
    abstract fun setAirSupply(supply: Int)

    @Shadow
    abstract fun teleportTo(x: Double, y: Double, z: Double)

    @Unique
    private var cachedVehicle: Entity? = null

    @Unique
    private var teleportReentry: Boolean = false

    @Unique
    private var airSupplyReentry: Boolean = false

    // ============ EntityCombustEvent ============

    @Inject(method = ["igniteForTicks"], at = [At("HEAD")], cancellable = true)
    private fun onIgniteForTicksPre(numberOfTicks: Int, ci: CallbackInfo) {
        val event = EntityCombustEvent.firePre(this as Entity, numberOfTicks) ?: run { ci.cancel(); return }
        // 如果事件处理器修改了着火时间，使用修改后的值
        if (event.duration != numberOfTicks) {
            if (getRemainingFireTicks() < event.duration) {
                setRemainingFireTicks(event.duration)
            }
            ci.cancel()
        }
    }

    @Inject(method = ["igniteForTicks"], at = [At("TAIL")])
    private fun onIgniteForTicksPost(numberOfTicks: Int, ci: CallbackInfo) {
        EntityCombustEvent.firePost(this as Entity, numberOfTicks)
    }

    // ============ EntityMountEvent / VehicleEnterEvent ============

    @Inject(method = ["startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z"], at = [At("HEAD")], cancellable = true)
    private fun onStartRidingPre(entityToRide: Entity, force: Boolean, sendEventAndTriggers: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        if (EntityMountEvent.firePre(this as Entity, entityToRide) == null) {
            cir.returnValue = false
            return
        }
        if (VehicleEnterEvent.firePre(this as Entity, entityToRide)) {
            cir.returnValue = false
        }
    }

    @Inject(method = ["startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z"], at = [At("RETURN")])
    private fun onStartRidingPost(entityToRide: Entity, force: Boolean, sendEventAndTriggers: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        if (cir.returnValue == true) {
            EntityMountEvent.firePost(this as Entity, entityToRide)
            VehicleEnterEvent.firePost(this as Entity, entityToRide)
        }
    }

    // ============ EntityDismountEvent / VehicleExitEvent ============

    @Inject(method = ["removeVehicle"], at = [At("HEAD")], cancellable = true)
    private fun onRemoveVehiclePre(ci: CallbackInfo) {
        val vehicle = getVehicle() ?: return
        cachedVehicle = vehicle
        if (EntityDismountEvent.firePre(this as Entity, vehicle) == null) {
            cachedVehicle = null
            ci.cancel()
            return
        }
        if (VehicleExitEvent.firePre(this as Entity, vehicle)) {
            cachedVehicle = null
            ci.cancel()
        }
    }

    @Inject(method = ["removeVehicle"], at = [At("TAIL")])
    private fun onRemoveVehiclePost(ci: CallbackInfo) {
        val vehicle = cachedVehicle ?: return
        cachedVehicle = null
        EntityDismountEvent.firePost(this as Entity, vehicle)
        VehicleExitEvent.firePost(this as Entity, vehicle)
    }

    // ============ EntityTeleportEvent ============

    @Inject(method = ["teleportTo(DDD)V"], at = [At("HEAD")], cancellable = true)
    private fun onTeleportToPre(x: Double, y: Double, z: Double, ci: CallbackInfo) {
        if (teleportReentry) return
        val self = this as Entity
        val event = EntityTeleportEvent.firePre(self, getX(), getY(), getZ(), x, y, z) ?: run { ci.cancel(); return }
        // 如果事件处理器修改了目标坐标，使用修改后的值
        if (event.toX != x || event.toY != y || event.toZ != z) {
            teleportReentry = true
            try {
                teleportTo(event.toX, event.toY, event.toZ)
            } finally {
                teleportReentry = false
            }
            ci.cancel()
        }
    }

    @Inject(method = ["teleportTo(DDD)V"], at = [At("TAIL")])
    private fun onTeleportToPost(x: Double, y: Double, z: Double, ci: CallbackInfo) {
        val self = this as Entity
        EntityTeleportEvent.firePost(self, self.x, self.y, self.z, x, y, z)
    }

    // ============ EntityAirChangeEvent ============

    @Inject(method = ["setAirSupply"], at = [At("HEAD")], cancellable = true)
    private fun onSetAirSupplyPre(supply: Int, ci: CallbackInfo) {
        if (airSupplyReentry) return
        val event = EntityAirChangeEvent.firePre(this as Entity, supply) ?: run { ci.cancel(); return }
        // 如果事件处理器修改了氧气值，使用修改后的值
        if (event.amount != supply) {
            airSupplyReentry = true
            try {
                setAirSupply(event.amount)
            } finally {
                airSupplyReentry = false
            }
            ci.cancel()
        }
    }

    @Inject(method = ["setAirSupply"], at = [At("TAIL")])
    private fun onSetAirSupplyPost(supply: Int, ci: CallbackInfo) {
        EntityAirChangeEvent.firePost(this as Entity, supply)
    }
}
