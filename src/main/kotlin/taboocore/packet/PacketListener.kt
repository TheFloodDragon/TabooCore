package taboocore.packet

import net.minecraft.network.protocol.Packet
import kotlin.reflect.KClass

/**
 * 数据包监听器句柄，用于注销监听
 */
class PacketListener internal constructor(
    /** 监听的数据包类型 */
    val packetType: KClass<out Packet<*>>,
    /** 监听方向（发送/接收） */
    val direction: Direction,
    /** 内部处理器引用 */
    internal val handler: Any
) {
    /**
     * 数据包方向
     */
    enum class Direction {
        /** 服务端发送给客户端 */
        SEND,
        /** 客户端发送给服务端 */
        RECEIVE
    }

    /**
     * 注销此监听器
     */
    fun unregister() {
        PacketManager.unregister(this)
    }
}
