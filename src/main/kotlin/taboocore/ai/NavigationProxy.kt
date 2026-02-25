package taboocore.ai

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.navigation.PathNavigation

/**
 * 寻路代理，封装 PathNavigation 的操作
 *
 * 使用示例：
 * ```kotlin
 * val mob: PathfinderMob = ...
 * mob.nav.moveTo(100.0, 64.0, 100.0, 1.2)
 * if (mob.nav.isDone) { ... }
 * ```
 */
class NavigationProxy(val mob: PathfinderMob) {

    /** 获取底层 PathNavigation */
    val navigation: PathNavigation
        get() = mob.navigation

    /** 移动到目标坐标 */
    fun moveTo(x: Double, y: Double, z: Double, speed: Double = 1.0): Boolean {
        return navigation.moveTo(x, y, z, speed)
    }

    /** 移动到目标方块位置 */
    fun moveTo(pos: BlockPos, speed: Double = 1.0): Boolean {
        return navigation.moveTo(pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5, speed)
    }

    /** 移动到目标实体 */
    fun moveTo(entity: Entity, speed: Double = 1.0): Boolean {
        return navigation.moveTo(entity, speed)
    }

    /** 停止移动 */
    fun stop() {
        navigation.stop()
    }

    /** 是否正在移动中 */
    val isMoving: Boolean
        get() = navigation.isInProgress

    /** 是否已到达目的地 */
    val isDone: Boolean
        get() = navigation.isDone
}

/**
 * 扩展属性：获取寻路代理
 */
val PathfinderMob.nav: NavigationProxy
    get() = NavigationProxy(this)
