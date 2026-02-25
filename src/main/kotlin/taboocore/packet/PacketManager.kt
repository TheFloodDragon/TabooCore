package taboocore.packet

import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import taboocore.util.ServerUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * 数据包管理器，提供数据包监听和发送 API
 */
object PacketManager {

    private val sendListeners = ConcurrentHashMap<KClass<out Packet<*>>, CopyOnWriteArrayList<(PacketSendEvent.Pre) -> Unit>>()
    private val receiveListeners = ConcurrentHashMap<KClass<out Packet<*>>, CopyOnWriteArrayList<(PacketReceiveEvent.Pre) -> Unit>>()

    /**
     * 注册发送数据包监听器
     *
     * @param type 要监听的数据包类型
     * @param handler 处理器函数
     * @return 监听器句柄，可用于注销
     */
    fun <T : Packet<*>> listenSend(type: KClass<T>, handler: (PacketSendEvent.Pre) -> Unit): PacketListener {
        val list = sendListeners.getOrPut(type) { CopyOnWriteArrayList() }
        list.add(handler)
        return PacketListener(type, PacketListener.Direction.SEND, handler)
    }

    /**
     * 注册接收数据包监听器
     *
     * @param type 要监听的数据包类型
     * @param handler 处理器函数
     * @return 监听器句柄，可用于注销
     */
    fun <T : Packet<*>> listenReceive(type: KClass<T>, handler: (PacketReceiveEvent.Pre) -> Unit): PacketListener {
        val list = receiveListeners.getOrPut(type) { CopyOnWriteArrayList() }
        list.add(handler)
        return PacketListener(type, PacketListener.Direction.RECEIVE, handler)
    }

    /**
     * 注销监听器
     *
     * @param listener 要注销的监听器句柄
     */
    fun unregister(listener: PacketListener) {
        when (listener.direction) {
            PacketListener.Direction.SEND -> {
                @Suppress("UNCHECKED_CAST")
                sendListeners[listener.packetType]?.remove(listener.handler as (PacketSendEvent.Pre) -> Unit)
            }
            PacketListener.Direction.RECEIVE -> {
                @Suppress("UNCHECKED_CAST")
                receiveListeners[listener.packetType]?.remove(listener.handler as (PacketReceiveEvent.Pre) -> Unit)
            }
        }
    }

    /**
     * 发送数据包给指定玩家
     *
     * @param player 目标玩家
     * @param packet 要发送的数据包
     */
    fun sendPacket(player: ServerPlayer, packet: Packet<*>) {
        player.connection.send(packet)
    }

    /**
     * 广播数据包给所有在线玩家
     *
     * @param packet 要广播的数据包
     */
    fun broadcastPacket(packet: Packet<*>) {
        val server = ServerUtils.serverInstance ?: return
        for (player in server.playerList.players) {
            player.connection.send(packet)
        }
    }

    /**
     * 内部使用：触发发送监听器
     */
    internal fun fireSendListeners(event: PacketSendEvent.Pre) {
        val list = sendListeners[event.packet::class] ?: return
        for (handler in list) {
            handler(event)
            if (event.isCancelled) return
        }
    }

    /**
     * 内部使用：触发接收监听器
     */
    internal fun fireReceiveListeners(event: PacketReceiveEvent.Pre) {
        val list = receiveListeners[event.packet::class] ?: return
        for (handler in list) {
            handler(event)
            if (event.isCancelled) return
        }
    }
}
