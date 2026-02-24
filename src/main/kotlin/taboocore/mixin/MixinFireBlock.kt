package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockBurnEvent
import taboocore.event.world.BlockIgniteEvent

@Mixin(FireBlock::class)
abstract class MixinFireBlock {

    /**
     * 拦截 checkBurnOut 方法，在方块被火焰烧毁前触发 BlockBurnEvent
     * checkBurnOut(Level level, BlockPos pos, int chance, RandomSource random, int age)
     */
    @Inject(method = ["checkBurnOut"], at = [At("HEAD")], cancellable = true)
    private fun onCheckBurnOut(
        level: Level,
        pos: BlockPos,
        chance: Int,
        random: RandomSource,
        age: Int,
        ci: CallbackInfo
    ) {
        if (level is ServerLevel) {
            val state = level.getBlockState(pos)
            if (!state.isAir) {
                if (BlockBurnEvent.fireBlockBurnPre(level, pos, state)) {
                    ci.cancel()
                }
            }
        }
    }

    /**
     * 拦截 checkBurnOut 方法尾部，方块被烧毁后触发 Post 事件
     * 注意：checkBurnOut 中可能不一定烧毁方块（有随机检测），
     * 所以 Post 事件在 TAIL 触发，此时需要检查方块状态是否变化
     */
    @Inject(method = ["checkBurnOut"], at = [At("TAIL")])
    private fun onCheckBurnOutPost(
        level: Level,
        pos: BlockPos,
        chance: Int,
        random: RandomSource,
        age: Int,
        ci: CallbackInfo
    ) {
        if (level is ServerLevel) {
            val state = level.getBlockState(pos)
            if (state.isAir) {
                BlockBurnEvent.fireBlockBurnPost(level, pos, state)
            }
        }
    }

    /**
     * 拦截 tick 方法，火焰蔓延时触发 BlockIgniteEvent
     * tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
     * 在 tick 方法的 HEAD 处拦截，由于火焰蔓延发生在 tick 内部的多个 setBlock 调用中，
     * 我们在整个 tick 方法开始时标记这是一次 SPREAD 类型的着火
     */
    @Inject(method = ["tick"], at = [At("HEAD")], cancellable = true)
    private fun onFireTick(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        random: RandomSource,
        ci: CallbackInfo
    ) {
        if (BlockIgniteEvent.fireBlockIgnitePre(level, pos, "SPREAD")) {
            ci.cancel()
        }
    }

    @Inject(method = ["tick"], at = [At("TAIL")])
    private fun onFireTickPost(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        random: RandomSource,
        ci: CallbackInfo
    ) {
        BlockIgniteEvent.fireBlockIgnitePost(level, pos, "SPREAD")
    }
}
