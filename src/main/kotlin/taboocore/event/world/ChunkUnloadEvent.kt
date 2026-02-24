package taboocore.event.world

import net.minecraft.world.level.chunk.LevelChunk
import taboolib.common.event.CancelableInternalEvent
import taboolib.common.event.InternalEvent

/**
 * 区块卸载事件
 */
class ChunkUnloadEvent {

    /**
     * 区块卸载前触发
     *
     * @property chunkX 区块 X 坐标
     * @property chunkZ 区块 Z 坐标
     * @property levelName 世界名称
     */
    class Pre(
        val chunkX: Int,
        val chunkZ: Int,
        val levelName: String
    ) : CancelableInternalEvent()

    /**
     * 区块卸载后触发
     *
     * @property chunkX 区块 X 坐标
     * @property chunkZ 区块 Z 坐标
     * @property levelName 世界名称
     */
    class Post(
        val chunkX: Int,
        val chunkZ: Int,
        val levelName: String
    ) : InternalEvent()

    companion object {
        /**
         * 区块卸载前触发，返回 true 表示事件被取消
         */
        fun fireChunkUnloadPre(chunk: LevelChunk): Boolean {
            val pos = chunk.pos
            val levelName = chunk.level.dimension().identifier().toString()
            val event = Pre(pos.x, pos.z, levelName)
            event.call()
            return event.isCancelled
        }

        /**
         * 区块卸载后触发
         */
        fun fireChunkUnloadPost(chunk: LevelChunk) {
            val pos = chunk.pos
            val levelName = chunk.level.dimension().identifier().toString()
            Post(pos.x, pos.z, levelName).call()
        }
    }
}
