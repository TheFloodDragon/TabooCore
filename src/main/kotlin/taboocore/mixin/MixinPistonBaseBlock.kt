package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.piston.PistonBaseBlock
import net.minecraft.world.level.block.piston.PistonStructureResolver
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.callback.LocalCapture
import taboocore.event.world.BlockPistonExtendEvent
import taboocore.event.world.BlockPistonRetractEvent

@Mixin(PistonBaseBlock::class)
abstract class MixinPistonBaseBlock {

    // ========== BlockPistonExtendEvent / BlockPistonRetractEvent ==========

    @Inject(method = ["moveBlocks"], at = [At("HEAD")], cancellable = true)
    private fun onMoveBlocksPre(level: Level, pistonPos: BlockPos, direction: Direction, extending: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        val resolver = PistonStructureResolver(level, pistonPos, direction, extending)
        if (!resolver.resolve()) return

        val blocks = resolver.toPush.toList()
        if (extending) {
            if (BlockPistonExtendEvent.firePre(pistonPos, blocks, direction)) {
                cir.returnValue = false
            }
        } else {
            if (BlockPistonRetractEvent.firePre(pistonPos, blocks, direction)) {
                cir.returnValue = false
            }
        }
    }

    @Inject(method = ["moveBlocks"], at = [At("RETURN")])
    private fun onMoveBlocksPost(level: Level, pistonPos: BlockPos, direction: Direction, extending: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        if (cir.returnValue != true) return
        // 无法在 RETURN 获取精确 blocks 列表，使用空列表触发 Post
        if (extending) {
            BlockPistonExtendEvent.firePost(pistonPos, emptyList(), direction)
        } else {
            BlockPistonRetractEvent.firePost(pistonPos, emptyList(), direction)
        }
    }
}
