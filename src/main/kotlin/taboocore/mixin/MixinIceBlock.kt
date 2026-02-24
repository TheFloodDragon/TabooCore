package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.IceBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockFadeEvent

@Mixin(IceBlock::class)
abstract class MixinIceBlock {

    @Unique
    private var fadeEvent: BlockFadeEvent.Pre? = null

    /**
     * 拦截 melt 方法，冰融化前触发 BlockFadeEvent
     * melt(BlockState state, Level level, BlockPos pos)
     */
    @Inject(method = ["melt"], at = [At("HEAD")], cancellable = true)
    private fun onMeltPre(state: BlockState, level: Level, pos: BlockPos, ci: CallbackInfo) {
        if (level is ServerLevel) {
            val newState = IceBlock.meltsInto()
            val event = BlockFadeEvent.firePre(level, pos, state, newState)
            if (event == null) {
                ci.cancel()
            } else {
                fadeEvent = event
                // 如果插件修改了 newState，在方块变化前应用
            }
        }
    }

    /**
     * 拦截 melt 方法尾部，冰融化后触发 Post 事件；如果 newState 被修改则应用
     */
    @Inject(method = ["melt"], at = [At("TAIL")])
    private fun onMeltPost(state: BlockState, level: Level, pos: BlockPos, ci: CallbackInfo) {
        if (level is ServerLevel) {
            val event = fadeEvent
            fadeEvent = null
            val currentState = level.getBlockState(pos)
            if (event != null && event.newState != IceBlock.meltsInto()) {
                // 插件修改了目标状态，覆盖当前方块
                level.setBlockAndUpdate(pos, event.newState)
                BlockFadeEvent.firePost(level, pos, state, event.newState)
            } else {
                BlockFadeEvent.firePost(level, pos, state, currentState)
            }
        }
    }
}
