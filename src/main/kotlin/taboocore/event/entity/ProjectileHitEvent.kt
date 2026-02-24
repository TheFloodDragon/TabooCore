package taboocore.event.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 抛射物命中事件
 */
class ProjectileHitEvent {

    /**
     * 抛射物命中前触发
     *
     * @property projectile 抛射物实体
     * @property hitResult 命中结果
     */
    class Pre(
        val projectile: Projectile,
        val hitResult: HitResult
    ) : CancelableInternalEvent()

    /**
     * 抛射物命中后触发
     *
     * @property projectile 抛射物实体
     * @property hitResult 命中结果
     */
    class Post(
        val projectile: Projectile,
        val hitResult: HitResult
    ) : InternalEvent()

    companion object {
        /**
         * 抛射物命中前触发，返回 true 表示事件被取消
         */
        fun firePre(projectile: Projectile, hitResult: HitResult): Boolean {
            val event = Pre(projectile, hitResult)
            event.call()
            return event.isCancelled
        }

        /**
         * 抛射物命中后触发
         */
        fun firePost(projectile: Projectile, hitResult: HitResult) {
            Post(projectile, hitResult).call()
        }
    }
}
