package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SnowLayerBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockFadeEvent

@Mixin(SnowLayerBlock::class)
abstract class MixinSnowLayerBlock {

    @Unique
    private var fadeEvent: BlockFadeEvent.Pre? = null

    /**
     * 拦截 randomTick 方法，雪层消融前触发 BlockFadeEvent
     * randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
     */
    @Inject(method = ["randomTick"], at = [At("HEAD")], cancellable = true)
    private fun onRandomTickPre(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        val newState = Blocks.AIR.defaultBlockState()
        val event = BlockFadeEvent.firePre(level, pos, state, newState)
        if (event == null) {
            ci.cancel()
        } else {
            fadeEvent = event
        }
    }

    /**
     * 拦截 randomTick 方法尾部，雪层消融后触发 Post 事件；如果 newState 被修改则应用
     */
    @Inject(method = ["randomTick"], at = [At("TAIL")])
    private fun onRandomTickPost(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        val event = fadeEvent
        fadeEvent = null
        val currentState = level.getBlockState(pos)
        if (currentState != state) {
            if (event != null && event.newState != Blocks.AIR.defaultBlockState()) {
                // 插件修改了目标状态，覆盖
                level.setBlockAndUpdate(pos, event.newState)
                BlockFadeEvent.firePost(level, pos, state, event.newState)
            } else {
                BlockFadeEvent.firePost(level, pos, state, currentState)
            }
        }
    }
}
