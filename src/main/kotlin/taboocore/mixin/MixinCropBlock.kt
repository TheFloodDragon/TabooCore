package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockGrowEvent

@Mixin(CropBlock::class)
abstract class MixinCropBlock {

    @Unique
    private var cachedOldState: BlockState? = null

    @Unique
    private var growEvent: BlockGrowEvent.Pre? = null

    /**
     * 拦截 randomTick 方法，农作物自然生长前触发 BlockGrowEvent
     * randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
     */
    @Inject(method = ["randomTick"], at = [At("HEAD")], cancellable = true)
    private fun onRandomTickPre(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        cachedOldState = state
    }

    /**
     * 拦截 randomTick 方法尾部，农作物自然生长后触发 Post 事件
     */
    @Inject(method = ["randomTick"], at = [At("TAIL")])
    private fun onRandomTickPost(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource, ci: CallbackInfo) {
        val oldState = cachedOldState ?: return
        cachedOldState = null
        val newState = level.getBlockState(pos)
        if (newState != oldState) {
            BlockGrowEvent.firePost(level, pos, oldState, newState)
        }
    }

    /**
     * 拦截 growCrops 方法（骨粉催熟），生长前触发 BlockGrowEvent
     * growCrops(Level level, BlockPos pos, BlockState state)
     */
    @Inject(method = ["growCrops"], at = [At("HEAD")], cancellable = true)
    private fun onGrowCropsPre(level: Level, pos: BlockPos, state: BlockState, ci: CallbackInfo) {
        if (level is ServerLevel) {
            val crop = this as Any as CropBlock
            val newAge = crop.maxAge.coerceAtMost(crop.getAge(state) + 1)
            val newState = crop.getStateForAge(newAge)
            val event = BlockGrowEvent.firePre(level, pos, state, newState)
            if (event == null) {
                ci.cancel()
            } else {
                growEvent = event
            }
        }
    }

    /**
     * 拦截 growCrops 方法尾部，生长后触发 Post 事件；如果 newState 被修改则应用
     */
    @Inject(method = ["growCrops"], at = [At("TAIL")])
    private fun onGrowCropsPost(level: Level, pos: BlockPos, state: BlockState, ci: CallbackInfo) {
        if (level is ServerLevel) {
            val event = growEvent
            growEvent = null
            val currentState = level.getBlockState(pos)
            if (currentState != state) {
                if (event != null && event.newState != currentState) {
                    // 插件修改了目标状态，覆盖
                    level.setBlock(pos, event.newState, 2)
                    BlockGrowEvent.firePost(level, pos, state, event.newState)
                } else {
                    BlockGrowEvent.firePost(level, pos, state, currentState)
                }
            }
        }
    }
}
