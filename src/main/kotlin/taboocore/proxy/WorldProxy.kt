package taboocore.proxy

import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

/**
 * 世界/维度代理，封装 ServerLevel 常用操作
 *
 * 提供方块操作、实体操作、天气控制、音效和粒子效果等功能
 */
class WorldProxy(val handle: ServerLevel) {

    /** 维度标识，例如 "minecraft:overworld" */
    val dimension: String
        get() = handle.dimension().identifier().toString()

    /** 是否白天（基于 overworld 时钟） */
    val isDay: Boolean
        get() {
            val time = handle.getOverworldClockTime() % 24000
            return time in 0..12999
        }

    /** 是否夜晚 */
    val isNight: Boolean
        get() = !isDay

    /** 是否正在下雨 */
    val isRaining: Boolean
        get() = handle.isRaining

    /** 是否正在打雷 */
    val isThundering: Boolean
        get() = handle.isThundering

    /** 游戏时间（tick） */
    val time: Long
        get() = handle.gameTime

    /** 世界难度 */
    var difficulty: Difficulty
        get() = handle.server.worldData.difficulty
        set(value) {
            handle.server.setDifficulty(value, false)
        }

    /** 当前维度内的所有玩家 */
    val players: List<ServerPlayer>
        get() = handle.players()

    /** 世界种子 */
    val seed: Long
        get() = handle.seed

    // ======================== 方块操作 ========================

    /**
     * 获取指定位置的方块状态
     * @param pos 方块坐标
     * @return 方块状态
     */
    fun getBlock(pos: BlockPos): BlockState {
        return handle.getBlockState(pos)
    }

    /**
     * 设置指定位置的方块
     * @param pos 方块坐标
     * @param state 方块状态
     * @param flags 更新标志，默认 3（通知邻居 + 发送客户端更新）
     */
    fun setBlock(pos: BlockPos, state: BlockState, flags: Int = 3) {
        handle.setBlock(pos, state, flags)
    }

    /**
     * 破坏指定位置的方块
     * @param pos 方块坐标
     * @param drop 是否掉落物品，默认 true
     * @return 是否成功破坏
     */
    fun breakBlock(pos: BlockPos, drop: Boolean = true): Boolean {
        return handle.destroyBlock(pos, drop, null, 512)
    }

    /**
     * 获取指定位置的方块实体
     * @param pos 方块坐标
     * @return 方块实体，不存在返回 null
     */
    fun getBlockEntity(pos: BlockPos): BlockEntity? {
        return handle.getBlockEntity(pos)
    }

    // ======================== 实体操作 ========================

    /**
     * 在指定位置生成实体
     * @param entity 要生成的实体
     * @param pos 生成位置
     * @return 生成的实体
     */
    fun spawnEntity(entity: Entity, pos: Vec3): Entity {
        entity.setPos(pos.x, pos.y, pos.z)
        handle.addFreshEntity(entity)
        return entity
    }

    /**
     * 获取指定区域内的所有实体
     * @param box 碰撞箱范围
     * @return 实体列表
     */
    fun getEntitiesInBox(box: AABB): List<Entity> {
        return handle.getEntities(null as Entity?, box) { true }
    }

    /**
     * 获取指定中心点和半径内的所有实体
     * @param center 中心点
     * @param radius 半径
     * @return 实体列表
     */
    fun getEntitiesInRadius(center: Vec3, radius: Double): List<Entity> {
        val box = AABB(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius, center.z + radius
        )
        return handle.getEntities(null as Entity?, box) { entity ->
            entity.distanceToSqr(center) <= radius * radius
        }
    }

    /**
     * 获取离指定位置最近的玩家
     * @param pos 位置
     * @param range 搜索范围
     * @return 最近的玩家，不存在返回 null
     */
    fun getNearestPlayer(pos: Vec3, range: Double): ServerPlayer? {
        return handle.getNearestPlayer(pos.x, pos.y, pos.z, range, false) as? ServerPlayer
    }

    // ======================== 音效/粒子效果 ========================

    /**
     * 播放音效
     * @param pos 位置
     * @param sound 音效事件
     * @param source 音效来源
     * @param volume 音量
     * @param pitch 音调
     */
    fun playSound(pos: Vec3, sound: SoundEvent, source: SoundSource, volume: Float, pitch: Float) {
        handle.playSound(null, pos.x, pos.y, pos.z, sound, source, volume, pitch)
    }

    /**
     * 在指定位置生成粒子效果
     * @param pos 位置
     * @param particle 粒子类型
     * @param count 粒子数量
     * @param spread 扩散范围
     */
    fun spawnParticle(pos: Vec3, particle: ParticleOptions, count: Int, spread: Double) {
        handle.sendParticles(particle, pos.x, pos.y, pos.z, count, spread, spread, spread, 0.0)
    }

    /**
     * 在指定位置生成闪电
     * @param pos 方块坐标
     * @param isSilent 是否为视觉效果（不造成伤害），默认 false
     */
    fun strikeLightning(pos: BlockPos, isSilent: Boolean = false) {
        val bolt = LightningBolt(EntityType.LIGHTNING_BOLT, handle)
        bolt.setPos(pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5)
        bolt.setVisualOnly(isSilent)
        handle.addFreshEntity(bolt)
    }

    // ======================== 时间/天气 ========================

    /**
     * 设置 overworld 时钟时间
     * @param time 时间（tick）
     */
    fun setTime(time: Long) {
        val overworldClock = handle.registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD)
        if (overworldClock.isPresent) {
            handle.clockManager().setTotalTicks(overworldClock.get(), time)
        }
    }

    /**
     * 设置天气
     * @param rain 是否下雨
     * @param thunder 是否打雷
     * @param duration 持续时间（tick），默认 6000
     */
    fun setWeather(rain: Boolean, thunder: Boolean, duration: Int = 6000) {
        val weatherData = handle.weatherData
        if (!rain && !thunder) {
            weatherData.clearWeatherTime = duration
            weatherData.rainTime = 0
            weatherData.isRaining = false
            weatherData.thunderTime = 0
            weatherData.isThundering = false
        } else {
            weatherData.clearWeatherTime = 0
            weatherData.rainTime = duration
            weatherData.isRaining = rain
            weatherData.thunderTime = if (thunder) duration else 0
            weatherData.isThundering = thunder
        }
    }
}

/**
 * 将 ServerLevel 包装为 WorldProxy
 */
fun ServerLevel.proxy(): WorldProxy = WorldProxy(this)
