package taboocore.event.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 抛射物发射事件
 */
class ProjectileLaunchEvent {

    /**
     * 抛射物发射前触发
     *
     * @property projectile 发射的抛射物
     * @property shooter 发射者（通过 projectile.getOwner() 获取，可能为 null）
     */
    class Pre(
        val projectile: Projectile,
        val shooter: Entity?
    ) : CancelableInternalEvent()

    /**
     * 抛射物发射后触发
     *
     * @property projectile 发射的抛射物
     * @property shooter 发射者（通过 projectile.getOwner() 获取，可能为 null）
     */
    class Post(
        val projectile: Projectile,
        val shooter: Entity?
    ) : InternalEvent()

    companion object {
        /**
         * 抛射物发射前触发，返回 null 表示事件被取消
         */
        fun firePre(projectile: Projectile): Pre? {
            val event = Pre(projectile, projectile.owner)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 抛射物发射后触发
         */
        fun firePost(projectile: Projectile) {
            Post(projectile, projectile.owner).call()
        }
    }
}
