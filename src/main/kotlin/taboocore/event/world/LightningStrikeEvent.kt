package taboocore.event.world

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LightningBolt
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 闪电击中事件
 */
class LightningStrikeEvent {

    /**
     * 闪电击中前触发
     *
     * @property level 所在世界
     * @property bolt 闪电实体
     * @property x 闪电 X 坐标
     * @property y 闪电 Y 坐标
     * @property z 闪电 Z 坐标
     */
    class Pre(
        val level: ServerLevel,
        val bolt: LightningBolt,
        val x: Double,
        val y: Double,
        val z: Double
    ) : CancelableInternalEvent()

    /**
     * 闪电击中后触发
     *
     * @property level 所在世界
     * @property bolt 闪电实体
     * @property x 闪电 X 坐标
     * @property y 闪电 Y 坐标
     * @property z 闪电 Z 坐标
     */
    class Post(
        val level: ServerLevel,
        val bolt: LightningBolt,
        val x: Double,
        val y: Double,
        val z: Double
    ) : InternalEvent()

    companion object {
        /**
         * 闪电击中前触发，返回 null 表示事件被取消
         */
        fun firePre(level: ServerLevel, bolt: LightningBolt): Pre? {
            val event = Pre(level, bolt, bolt.x, bolt.y, bolt.z)
            event.call()
            return if (event.isCancelled) null else event
        }

        /**
         * 闪电击中后触发
         */
        fun firePost(level: ServerLevel, bolt: LightningBolt) {
            Post(level, bolt, bolt.x, bolt.y, bolt.z).call()
        }
    }
}
