package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.FluidState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockFromToEvent

@Mixin(FlowingFluid::class)
abstract class MixinFlowingFluid {

    /**
     * 拦截 spreadTo 方法，液体流动到目标位置前触发 BlockFromToEvent
     * spreadTo(LevelAccessor level, BlockPos pos, BlockState state, Direction direction, FluidState target)
     */
    @Inject(method = ["spreadTo"], at = [At("HEAD")], cancellable = true)
    private fun onSpreadToPre(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        direction: Direction,
        target: FluidState,
        ci: CallbackInfo
    ) {
        if (level is ServerLevel) {
            val fromPos = pos.relative(direction.opposite)
            val fluid = target.createLegacyBlock()
            val event = BlockFromToEvent.firePre(level, fromPos, pos, fluid)
            if (event == null) {
                ci.cancel()
            }
            // fluid 修改在 spreadTo 中难以应用（FluidState 已确定），仅支持取消
        }
    }

    /**
     * 拦截 spreadTo 方法尾部，液体流动后触发 Post 事件
     */
    @Inject(method = ["spreadTo"], at = [At("TAIL")])
    private fun onSpreadToPost(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        direction: Direction,
        target: FluidState,
        ci: CallbackInfo
    ) {
        if (level is ServerLevel) {
            val fromPos = pos.relative(direction.opposite)
            val fluid = target.createLegacyBlock()
            BlockFromToEvent.firePost(level, fromPos, pos, fluid)
        }
    }
}
