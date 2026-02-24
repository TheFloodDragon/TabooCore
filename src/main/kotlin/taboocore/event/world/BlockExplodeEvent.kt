package taboocore.event.world

import net.minecraft.world.phys.Vec3
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 方块爆炸事件
 */
class BlockExplodeEvent {

    /**
     * 爆炸发生前触发
     *
     * @property x 爆炸中心 X 坐标
     * @property y 爆炸中心 Y 坐标
     * @property z 爆炸中心 Z 坐标
     * @property radius 爆炸半径
     */
    class Pre(
        val x: Double,
        val y: Double,
        val z: Double,
        val radius: Float
    ) : CancelableInternalEvent()

    /**
     * 爆炸发生后触发
     *
     * @property x 爆炸中心 X 坐标
     * @property y 爆炸中心 Y 坐标
     * @property z 爆炸中心 Z 坐标
     * @property radius 爆炸半径
     * @property affectedBlockCount 受影响的方块数量
     */
    class Post(
        val x: Double,
        val y: Double,
        val z: Double,
        val radius: Float,
        val affectedBlockCount: Int
    ) : InternalEvent()

    companion object {
        /**
         * 爆炸发生前触发，返回 true 表示事件被取消
         */
        fun fireBlockExplodePre(center: Vec3, radius: Float): Boolean {
            val event = Pre(center.x, center.y, center.z, radius)
            event.call()
            return event.isCancelled
        }

        /**
         * 爆炸发生后触发
         */
        fun fireBlockExplodePost(center: Vec3, radius: Float, affectedBlockCount: Int) {
            Post(center.x, center.y, center.z, radius, affectedBlockCount).call()
        }
    }
}
