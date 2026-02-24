package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.LeavesDecayEvent

@Mixin(LeavesBlock::class)
abstract class MixinLeavesBlock {

    /**
     * 拦截 randomTick 方法，树叶自然凋落前触发 LeavesDecayEvent
     * randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
     */
    @Inject(method = ["randomTick"], at = [At("HEAD")], cancellable = true)
    private fun onRandomTickPre(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        val event = LeavesDecayEvent.firePre(level, pos, state)
        if (event == null) {
            ci.cancel()
        }
    }

    /**
     * 拦截 randomTick 方法尾部，树叶凋落后触发 Post 事件
     */
    @Inject(method = ["randomTick"], at = [At("TAIL")])
    private fun onRandomTickPost(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        // 只有方块已被移除（变为空气）时才触发 Post
        val currentState = level.getBlockState(pos)
        if (currentState != state) {
            LeavesDecayEvent.firePost(level, pos, state)
        }
    }
}
