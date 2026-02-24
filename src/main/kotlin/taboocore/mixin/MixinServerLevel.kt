package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.level.chunk.LevelChunk
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.entity.EntitySpawnEvent
import taboocore.event.world.BlockFormEvent
import taboocore.event.world.ChunkLoadEvent
import taboocore.event.world.ChunkUnloadEvent
import taboocore.event.world.LightningStrikeEvent
import taboocore.event.world.WeatherChangeEvent

@Mixin(ServerLevel::class)
abstract class MixinServerLevel {

    @Unique
    private var wasRaining: Boolean = false

    @Unique
    private var wasThundering: Boolean = false

    // ============ WeatherChangeEvent ============

    /**
     * 拦截 advanceWeatherCycle 方法，捕获天气变化前的状态
     */
    @Inject(method = ["advanceWeatherCycle"], at = [At("HEAD")])
    private fun onAdvanceWeatherCyclePre(ci: CallbackInfo) {
        val level = this as Any as ServerLevel
        wasRaining = level.isRaining
        wasThundering = level.isThundering
    }

    /**
     * 拦截 advanceWeatherCycle 方法尾部，如果天气状态发生了变化则触发事件
     */
    @Inject(method = ["advanceWeatherCycle"], at = [At("TAIL")])
    private fun onAdvanceWeatherCyclePost(ci: CallbackInfo) {
        val level = this as Any as ServerLevel
        val nowRaining = level.isRaining
        val nowThundering = level.isThundering
        if (wasRaining != nowRaining || wasThundering != nowThundering) {
            if (WeatherChangeEvent.fireWeatherChangePre(level, nowRaining, nowThundering)) {
                // 无法真正取消天气变化（已经发生），但仍然触发 Pre 让插件知道
                // 如果需要取消，插件应在 Pre 中使用 resetWeatherCycle 或设置回原值
            }
            WeatherChangeEvent.fireWeatherChangePost(level, nowRaining, nowThundering)
        }
    }

    // ============ ChunkLoadEvent ============

    @Inject(method = ["startTickingChunk"], at = [At("HEAD")])
    private fun onChunkLoadPre(levelChunk: LevelChunk, ci: CallbackInfo) {
        ChunkLoadEvent.fireChunkLoadPre(levelChunk)
    }

    @Inject(method = ["startTickingChunk"], at = [At("TAIL")])
    private fun onChunkLoadPost(levelChunk: LevelChunk, ci: CallbackInfo) {
        ChunkLoadEvent.fireChunkLoadPost(levelChunk)
    }

    // ============ ChunkUnloadEvent ============

    @Inject(method = ["unload"], at = [At("HEAD")], cancellable = true)
    private fun onChunkUnloadPre(levelChunk: LevelChunk, ci: CallbackInfo) {
        if (ChunkUnloadEvent.fireChunkUnloadPre(levelChunk)) {
            ci.cancel()
        }
    }

    @Inject(method = ["unload"], at = [At("TAIL")])
    private fun onChunkUnloadPost(levelChunk: LevelChunk, ci: CallbackInfo) {
        ChunkUnloadEvent.fireChunkUnloadPost(levelChunk)
    }

    // ============ EntitySpawnEvent ============

    @Inject(method = ["addFreshEntity"], at = [At("HEAD")], cancellable = true)
    private fun onAddFreshEntityPre(entity: Entity, cir: CallbackInfoReturnable<Boolean>) {
        // LightningStrikeEvent
        if (entity is LightningBolt) {
            val level = this as Any as ServerLevel
            val event = LightningStrikeEvent.firePre(level, entity)
            if (event == null) {
                cir.returnValue = false
                return
            }
        }
        if (EntitySpawnEvent.firePre(entity)) {
            cir.returnValue = false
        }
    }

    @Inject(method = ["addFreshEntity"], at = [At("RETURN")])
    private fun onAddFreshEntityPost(entity: Entity, cir: CallbackInfoReturnable<Boolean>) {
        if (cir.returnValue == true) {
            // LightningStrikeEvent Post
            if (entity is LightningBolt) {
                val level = this as Any as ServerLevel
                LightningStrikeEvent.firePost(level, entity)
            }
            EntitySpawnEvent.firePost(entity)
        }
    }

    // ============ BlockFormEvent (snow/ice from precipitation) ============

    @Unique
    private var precipitationOldState: net.minecraft.world.level.block.state.BlockState? = null

    /**
     * 拦截 tickPrecipitation 方法，降水导致雪/冰形成前捕获旧状态
     * tickPrecipitation(BlockPos pos)
     */
    @Inject(method = ["tickPrecipitation"], at = [At("HEAD")], cancellable = true)
    private fun onTickPrecipitationPre(pos: BlockPos, ci: CallbackInfo) {
        val level = this as Any as ServerLevel
        val topPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos)
        val belowPos = topPos.below()
        // 记录可能被修改的位置的旧状态
        precipitationOldState = level.getBlockState(belowPos)
    }

    /**
     * 拦截 tickPrecipitation 方法尾部，如果发生了冰/雪形成则触发 BlockFormEvent
     */
    @Inject(method = ["tickPrecipitation"], at = [At("TAIL")])
    private fun onTickPrecipitationPost(pos: BlockPos, ci: CallbackInfo) {
        val level = this as Any as ServerLevel
        val topPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos)
        val belowPos = topPos.below()
        val oldState = precipitationOldState
        precipitationOldState = null
        if (oldState != null) {
            val newBelowState = level.getBlockState(belowPos)
            if (newBelowState != oldState) {
                BlockFormEvent.firePost(level, belowPos, oldState, newBelowState)
            }
            // 也检查顶部位置（雪层堆积）
            val newTopState = level.getBlockState(topPos)
            // 注意：如果顶部位置也发生了变化（雪层），单独触发
        }
    }
}
