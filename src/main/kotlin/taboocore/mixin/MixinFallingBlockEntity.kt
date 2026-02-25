package taboocore.mixin

import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.entity.EntityChangeBlockEvent

@Mixin(FallingBlockEntity::class)
abstract class MixinFallingBlockEntity {

    @Shadow
    private lateinit var blockState: BlockState

    @Unique
    private var changeBlockFired: Boolean = false

    // ========== EntityChangeBlockEvent (下落方块着地) ==========

    @Inject(method = ["tick"], at = [At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
        ordinal = 0
    )], cancellable = true)
    private fun onTickSetBlockPre(ci: CallbackInfo) {
        val self = this as FallingBlockEntity
        val pos = self.blockPosition()
        val oldState = self.level().getBlockState(pos)
        changeBlockFired = false
        if (EntityChangeBlockEvent.firePre(self, pos, oldState, blockState)) {
            self.discard()
            ci.cancel()
            return
        }
        changeBlockFired = true
    }
}
