package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.portal.PortalShape
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.PortalCreateEvent

@Mixin(PortalShape::class)
abstract class MixinPortalShape {

    @Shadow
    @Final
    private lateinit var bottomLeft: BlockPos

    @Shadow
    @Final
    private var width: Int = 0

    @Shadow
    @Final
    private var height: Int = 0

    @Shadow
    @Final
    private lateinit var rightDir: Direction

    // ========== PortalCreateEvent ==========

    @Inject(method = ["createPortalBlocks"], at = [At("HEAD")], cancellable = true)
    private fun onCreatePortalBlocksPre(level: LevelAccessor, ci: CallbackInfo) {
        val blocks = mutableListOf<BlockPos>()
        val topRight = bottomLeft.relative(Direction.UP, height - 1).relative(rightDir, width - 1)
        BlockPos.betweenClosed(bottomLeft, topRight).forEach { pos ->
            blocks.add(pos.immutable())
        }

        if (PortalCreateEvent.firePre(blocks, "NETHER_PAIR", null)) {
            ci.cancel()
        }
    }

    @Inject(method = ["createPortalBlocks"], at = [At("RETURN")])
    private fun onCreatePortalBlocksPost(level: LevelAccessor, ci: CallbackInfo) {
        val blocks = mutableListOf<BlockPos>()
        val topRight = bottomLeft.relative(Direction.UP, height - 1).relative(rightDir, width - 1)
        BlockPos.betweenClosed(bottomLeft, topRight).forEach { pos ->
            blocks.add(pos.immutable())
        }

        PortalCreateEvent.firePost(blocks, "NETHER_PAIR", null)
    }
}
