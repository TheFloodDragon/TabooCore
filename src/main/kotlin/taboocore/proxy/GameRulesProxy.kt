package taboocore.proxy

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.gamerules.GameRule
import net.minecraft.world.level.gamerules.GameRules

/**
 * 游戏规则代理，提供类型安全的游戏规则访问
 *
 * 使用示例：
 * ```kotlin
 * val rules = level.gameRulesProxy
 * rules.keepInventory = true
 * rules.randomTickSpeed = 10
 * rules.pvp = false
 * ```
 */
class GameRulesProxy(val handle: ServerLevel) {

    private val rules: GameRules
        get() = handle.gameRules

    private val server
        get() = handle.server

    // ======================== 布尔值规则 ========================

    /** 是否推进日夜循环 */
    var doDaylightCycle: Boolean
        get() = rules.get(GameRules.ADVANCE_TIME) as Boolean
        set(value) = rules.set(GameRules.ADVANCE_TIME, value, server)

    /** 是否推进天气变化 */
    var doWeatherCycle: Boolean
        get() = rules.get(GameRules.ADVANCE_WEATHER) as Boolean
        set(value) = rules.set(GameRules.ADVANCE_WEATHER, value, server)

    /** 是否启用火焰蔓延 */
    var doFireTick: Boolean
        get() {
            // 火焰蔓延通过 FIRE_SPREAD_RADIUS_AROUND_PLAYER 控制，> 0 表示启用
            return (rules.get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) as Int) > 0
        }
        set(value) = rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, if (value) 128 else 0, server)

    /** 是否生成怪物 */
    var doMobSpawning: Boolean
        get() = rules.get(GameRules.SPAWN_MOBS) as Boolean
        set(value) = rules.set(GameRules.SPAWN_MOBS, value, server)

    /** 怪物是否掉落战利品 */
    var doMobLoot: Boolean
        get() = rules.get(GameRules.MOB_DROPS) as Boolean
        set(value) = rules.set(GameRules.MOB_DROPS, value, server)

    /** 实体是否掉落物品（非怪物） */
    var doEntityDrops: Boolean
        get() = rules.get(GameRules.ENTITY_DROPS) as Boolean
        set(value) = rules.set(GameRules.ENTITY_DROPS, value, server)

    /** 方块是否掉落物品 */
    var doTileDrops: Boolean
        get() = rules.get(GameRules.BLOCK_DROPS) as Boolean
        set(value) = rules.set(GameRules.BLOCK_DROPS, value, server)

    /** 死亡时是否保留背包物品 */
    var keepInventory: Boolean
        get() = rules.get(GameRules.KEEP_INVENTORY) as Boolean
        set(value) = rules.set(GameRules.KEEP_INVENTORY, value, server)

    /** 怪物是否可以破坏方块 */
    var mobGriefing: Boolean
        get() = rules.get(GameRules.MOB_GRIEFING) as Boolean
        set(value) = rules.set(GameRules.MOB_GRIEFING, value, server)

    /** 是否自然回血 */
    var naturalRegeneration: Boolean
        get() = rules.get(GameRules.NATURAL_HEALTH_REGENERATION) as Boolean
        set(value) = rules.set(GameRules.NATURAL_HEALTH_REGENERATION, value, server)

    /** 是否允许 PVP */
    var pvp: Boolean
        get() = rules.get(GameRules.PVP) as Boolean
        set(value) = rules.set(GameRules.PVP, value, server)

    /** 是否显示成就消息 */
    var announceAdvancements: Boolean
        get() = rules.get(GameRules.SHOW_ADVANCEMENT_MESSAGES) as Boolean
        set(value) = rules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, value, server)

    /** 是否显示死亡消息 */
    var showDeathMessages: Boolean
        get() = rules.get(GameRules.SHOW_DEATH_MESSAGES) as Boolean
        set(value) = rules.set(GameRules.SHOW_DEATH_MESSAGES, value, server)

    /** 是否发送命令反馈 */
    var sendCommandFeedback: Boolean
        get() = rules.get(GameRules.SEND_COMMAND_FEEDBACK) as Boolean
        set(value) = rules.set(GameRules.SEND_COMMAND_FEEDBACK, value, server)

    // ======================== 整数值规则 ========================

    /** 随机刻速度 */
    var randomTickSpeed: Int
        get() = rules.get(GameRules.RANDOM_TICK_SPEED) as Int
        set(value) = rules.set(GameRules.RANDOM_TICK_SPEED, value, server)

    /** 重生半径 */
    var spawnRadius: Int
        get() = rules.get(GameRules.RESPAWN_RADIUS) as Int
        set(value) = rules.set(GameRules.RESPAWN_RADIUS, value, server)

    /** 实体最大推挤数 */
    var maxEntityCramming: Int
        get() = rules.get(GameRules.MAX_ENTITY_CRAMMING) as Int
        set(value) = rules.set(GameRules.MAX_ENTITY_CRAMMING, value, server)

    // ======================== 通用访问 ========================

    /**
     * 获取指定游戏规则的值
     * @param rule 游戏规则
     * @return 规则值
     */
    fun <T : Any> get(rule: GameRule<T>): T {
        return rules.get(rule)
    }

    /**
     * 设置布尔类型的游戏规则
     * @param rule 游戏规则
     * @param value 值
     */
    fun setBoolean(rule: GameRule<Boolean>, value: Boolean) {
        rules.set(rule, value, server)
    }

    /**
     * 设置整数类型的游戏规则
     * @param rule 游戏规则
     * @param value 值
     */
    fun setInt(rule: GameRule<Int>, value: Int) {
        rules.set(rule, value, server)
    }
}

/**
 * 获取世界的游戏规则代理
 */
val ServerLevel.gameRulesProxy: GameRulesProxy
    get() = GameRulesProxy(this)
