package taboocore.event.entity

import net.minecraft.world.entity.Entity
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 实体爆炸事件
 */
class EntityExplodeEvent {

    /**
     * 实体爆炸前触发
     *
     * @property entity 引发爆炸的实体（可能为 null）
     * @property x 爆炸中心 X 坐标
     * @property y 爆炸中心 Y 坐标
     * @property z 爆炸中心 Z 坐标
     * @property radius 爆炸半径
     */
    class Pre(
        val entity: Entity?,
        val x: Double,
        val y: Double,
        val z: Double,
        val radius: Float
    ) : CancelableInternalEvent()

    /**
     * 实体爆炸后触发
     *
     * @property entity 引发爆炸的实体（可能为 null）
     * @property x 爆炸中心 X 坐标
     * @property y 爆炸中心 Y 坐标
     * @property z 爆炸中心 Z 坐标
     * @property radius 爆炸半径
     */
    class Post(
        val entity: Entity?,
        val x: Double,
        val y: Double,
        val z: Double,
        val radius: Float
    ) : InternalEvent()

    companion object {
        /**
         * 实体爆炸前触发，返回 true 表示事件被取消
         */
        fun firePre(entity: Entity?, x: Double, y: Double, z: Double, radius: Float): Boolean {
            val event = Pre(entity, x, y, z, radius)
            event.call()
            return event.isCancelled
        }

        /**
         * 实体爆炸后触发
         */
        fun firePost(entity: Entity?, x: Double, y: Double, z: Double, radius: Float) {
            Post(entity, x, y, z, radius).call()
        }
    }
}
