package taboocore.ai

import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.GoalSelector
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * AI 目标代理，封装 GoalSelector 的操作
 *
 * 使用示例：
 * ```kotlin
 * zombie.ai {
 *     clearGoals()
 *     addGoal(1, FloatGoal(zombie))
 *     addGoal(2, MeleeAttackGoal(zombie, 1.0, false))
 *     addTarget(1, NearestAttackableTargetGoal(zombie, Player::class.java, true))
 * }
 * ```
 */
class GoalProxy(val mob: Mob) {

    /** 获取 goalSelector */
    val goalSelector: GoalSelector = Mob::class.memberProperties
        .first { it.name == "goalSelector" }
        .apply { isAccessible = true }
        .get(mob) as GoalSelector

    /** 获取 targetSelector */
    val targetSelector: GoalSelector = Mob::class.memberProperties
        .first { it.name == "targetSelector" }
        .apply { isAccessible = true }
        .get(mob) as GoalSelector

    /** 添加行为目标 */
    fun addGoal(priority: Int, goal: Goal) {
        goalSelector.addGoal(priority, goal)
    }

    /** 添加目标选择器 */
    fun addTarget(priority: Int, goal: Goal) {
        targetSelector.addGoal(priority, goal)
    }

    /** 移除特定类型的行为目标 */
    fun <T : Goal> removeGoal(type: KClass<T>) {
        goalSelector.removeAllGoals { type.java.isInstance(it) }
    }

    /** 移除特定类型的目标选择器 */
    fun <T : Goal> removeTarget(type: KClass<T>) {
        targetSelector.removeAllGoals { type.java.isInstance(it) }
    }

    /** 清除所有行为目标 */
    fun clearGoals() {
        goalSelector.removeAllGoals { true }
    }

    /** 清除所有目标选择器 */
    fun clearTargets() {
        targetSelector.removeAllGoals { true }
    }

    /** 获取当前所有行为目标 */
    val goals: List<Goal>
        get() = goalSelector.availableGoals.map { it.goal }

    /** 获取当前所有目标选择器 */
    val targets: List<Goal>
        get() = targetSelector.availableGoals.map { it.goal }
}

/**
 * 扩展函数：配置生物 AI
 * 所有 Mob 子类（Animal, PathfinderMob 等）均可使用
 */
fun Mob.ai(block: GoalProxy.() -> Unit): GoalProxy {
    return GoalProxy(this).apply(block)
}
