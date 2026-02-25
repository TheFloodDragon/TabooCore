package taboocore.mixin

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.entity.EntityBreedEvent
import taboocore.event.entity.EntityEnterLoveModeEvent

@Mixin(Animal::class)
abstract class MixinAnimal {

    // ========== EntityEnterLoveModeEvent ==========

    @Inject(method = ["setInLove"], at = [At("HEAD")], cancellable = true)
    private fun onSetInLovePre(player: Player, ci: CallbackInfo) {
        if (EntityEnterLoveModeEvent.firePre(this as Animal, player)) {
            ci.cancel()
        }
    }

    @Inject(method = ["setInLove"], at = [At("RETURN")])
    private fun onSetInLovePost(player: Player, ci: CallbackInfo) {
        EntityEnterLoveModeEvent.firePost(this as Animal, player)
    }

    // ========== EntityBreedEvent ==========

    @Unique
    private var breedPartner: Animal? = null

    @Unique
    private var breedFired: Boolean = false

    @Inject(method = ["spawnChildFromBreeding"], at = [At("HEAD")], cancellable = true)
    private fun onSpawnChildFromBreedingPre(level: ServerLevel, partner: Animal, ci: CallbackInfo) {
        breedFired = false
        breedPartner = partner
        if (EntityBreedEvent.firePre(this as Animal, partner, null)) {
            breedPartner = null
            ci.cancel()
            return
        }
        breedFired = true
    }

    @Inject(method = ["spawnChildFromBreeding"], at = [At("RETURN")])
    private fun onSpawnChildFromBreedingPost(level: ServerLevel, partner: Animal, ci: CallbackInfo) {
        if (!breedFired) return
        breedFired = false
        val p = breedPartner ?: return
        breedPartner = null
        EntityBreedEvent.firePost(this as Animal, p, null)
    }
}
