package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockFormEvent

@Mixin(LiquidBlock::class)
abstract class MixinLiquidBlock {

    @Unique
    private var formOldState: BlockState? = null

    /**
     * 拦截 onPlace 方法，当液体放置时可能触发 shouldSpreadLiquid（形成黑曜石/圆石）
     * onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
     */
    @Inject(method = ["onPlace"], at = [At("HEAD")])
    private fun onPlacePre(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean, ci: CallbackInfo) {
        if (level is ServerLevel) {
            formOldState = state
        }
    }

    /**
     * 拦截 onPlace 方法尾部，检查方块是否由液体变为固体（黑曜石/圆石/玄武岩）
     */
    @Inject(method = ["onPlace"], at = [At("TAIL")])
    private fun onPlacePost(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean, ci: CallbackInfo) {
        if (level is ServerLevel) {
            val old = formOldState
            formOldState = null
            if (old != null) {
                val newState = level.getBlockState(pos)
                // 如果方块从液体变成了固体（黑曜石、圆石、玄武岩）
                if (newState != old && !newState.fluidState.isEmpty.not()) {
                    val block = newState.block
                    if (block == Blocks.OBSIDIAN || block == Blocks.COBBLESTONE || block == Blocks.BASALT) {
                        BlockFormEvent.firePost(level, pos, old, newState)
                    }
                }
            }
        }
    }
}
