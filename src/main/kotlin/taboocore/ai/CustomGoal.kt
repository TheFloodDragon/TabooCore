package taboocore.ai

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import java.util.EnumSet

/**
 * 自定义 AI 目标基础类，通过 Kotlin lambda 实现
 * Goal 本身不强制持有实体引用，调用者通过闭包捕获实体
 *
 * 使用示例：
 * ```kotlin
 * zombie.ai {
 *     addGoal(5, customGoal {
 *         canUse { zombie.health < zombie.maxHealth * 0.5f }
 *         tick { zombie.heal(0.1f) }
 *         flags(Goal.Flag.MOVE)
 *     })
 * }
 * ```
 */
class CustomGoal : Goal() {

    private var canUseCheck: (() -> Boolean)? = null
    private var canContinueCheck: (() -> Boolean)? = null
    private var onStart: (() -> Unit)? = null
    private var onStop: (() -> Unit)? = null
    private var onTick: (() -> Unit)? = null
    private var goalFlags: EnumSet<Flag> = EnumSet.noneOf(Flag::class.java)
    private var requiresEveryTick: Boolean = false

    /** 设置是否可用的检查 */
    fun canUse(check: () -> Boolean): CustomGoal {
        canUseCheck = check
        return this
    }

    /** 设置是否可继续使用的检查 */
    fun canContinueToUse(check: () -> Boolean): CustomGoal {
        canContinueCheck = check
        return this
    }

    /** 设置开始时的回调 */
    fun onStart(action: () -> Unit): CustomGoal {
        onStart = action
        return this
    }

    /** 设置停止时的回调 */
    fun onStop(action: () -> Unit): CustomGoal {
        onStop = action
        return this
    }

    /** 设置每 tick 的回调 */
    fun tick(action: () -> Unit): CustomGoal {
        onTick = action
        return this
    }

    /** 设置控制标志 */
    fun flags(vararg flag: Flag): CustomGoal {
        goalFlags = if (flag.isEmpty()) {
            EnumSet.noneOf(Flag::class.java)
        } else {
            EnumSet.of(flag.first(), *flag.drop(1).toTypedArray())
        }
        return this
    }

    /** 设置是否需要每 tick 更新 */
    fun requiresEveryTick(value: Boolean = true): CustomGoal {
        requiresEveryTick = value
        return this
    }

    override fun canUse(): Boolean = canUseCheck?.invoke() ?: false

    override fun canContinueToUse(): Boolean = canContinueCheck?.invoke() ?: super.canContinueToUse()

    override fun start() {
        onStart?.invoke()
    }

    override fun stop() {
        onStop?.invoke()
    }

    override fun tick() {
        onTick?.invoke()
    }

    override fun getFlags(): EnumSet<Flag> = goalFlags

    override fun requiresUpdateEveryTick(): Boolean = requiresEveryTick
}

/**
 * 创建自定义 AI 目标，通过闭包捕获实体引用
 */
fun customGoal(block: CustomGoal.() -> Unit): CustomGoal {
    return CustomGoal().apply(block)
}

/**
 * 自定义目标选择器，基于 TargetGoal
 * TargetGoal 构造需要 Mob，此处保留 mob 参数
 *
 * 使用示例：
 * ```kotlin
 * val mob: PathfinderMob = ...
 * mob.ai {
 *     addTarget(1, customTargetGoal(mob) {
 *         canUse { mob.health > 10f }
 *         findTarget { mob.level().getNearestPlayer(mob, 16.0) }
 *     })
 * }
 * ```
 */
class CustomTargetGoal(mob: PathfinderMob) : TargetGoal(mob, false) {

    private var canUseCheck: (() -> Boolean)? = null
    private var targetProvider: (() -> LivingEntity?)? = null

    /** 设置是否可用的检查 */
    fun canUse(check: () -> Boolean): CustomTargetGoal {
        canUseCheck = check
        return this
    }

    /** 设置目标提供者 */
    fun findTarget(provider: () -> LivingEntity?): CustomTargetGoal {
        targetProvider = provider
        return this
    }

    override fun canUse(): Boolean {
        if (canUseCheck?.invoke() == false) return false
        val target = targetProvider?.invoke() ?: return false
        targetMob = target
        return true
    }

    override fun start() {
        mob.target = targetMob
        super.start()
    }
}

/**
 * 创建自定义目标选择器
 * TargetGoal 需要 PathfinderMob 参数（用于寻路和距离检测）
 */
fun customTargetGoal(mob: PathfinderMob, block: CustomTargetGoal.() -> Unit): CustomTargetGoal {
    return CustomTargetGoal(mob).apply(block)
}
