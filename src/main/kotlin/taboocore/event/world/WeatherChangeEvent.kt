package taboocore.event.world

import net.minecraft.server.level.ServerLevel
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 天气变化事件
 */
class WeatherChangeEvent {

    /**
     * 天气变化前触发
     *
     * @property levelName 世界名称
     * @property raining 是否正在下雨
     * @property thundering 是否正在打雷
     */
    class Pre(
        val levelName: String,
        val raining: Boolean,
        val thundering: Boolean
    ) : CancelableInternalEvent()

    /**
     * 天气变化后触发
     *
     * @property levelName 世界名称
     * @property raining 是否正在下雨
     * @property thundering 是否正在打雷
     */
    class Post(
        val levelName: String,
        val raining: Boolean,
        val thundering: Boolean
    ) : InternalEvent()

    companion object {
        /**
         * 天气变化前触发，返回 true 表示事件被取消
         */
        fun fireWeatherChangePre(level: ServerLevel, raining: Boolean, thundering: Boolean): Boolean {
            val levelName = level.dimension().identifier().toString()
            val event = Pre(levelName, raining, thundering)
            event.call()
            return event.isCancelled
        }

        /**
         * 天气变化后触发
         */
        fun fireWeatherChangePost(level: ServerLevel, raining: Boolean, thundering: Boolean) {
            val levelName = level.dimension().identifier().toString()
            Post(levelName, raining, thundering).call()
        }
    }
}
