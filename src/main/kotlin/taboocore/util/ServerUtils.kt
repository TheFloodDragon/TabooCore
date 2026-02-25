package taboocore.util

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.Identifier
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * 服务端工具
 * 提供获取服务器实例、在线玩家、广播消息等常用操作
 */
object ServerUtils {

    /** 服务端实例引用，由启动流程设置 */
    @Volatile
    var serverInstance: MinecraftServer? = null

    /**
     * 获取当前服务端实例
     * @throws IllegalStateException 服务端尚未初始化
     */
    val server: MinecraftServer
        get() = serverInstance ?: error("MinecraftServer 尚未初始化")

    /**
     * 获取所有在线玩家
     */
    val onlinePlayers: List<ServerPlayer>
        get() = server.playerList.players

    /**
     * 获取当前服务端 tick 计数
     */
    val ticks: Long
        get() = server.tickCount.toLong()

    /**
     * 向所有在线玩家广播纯文本消息
     * @param message 消息内容
     */
    @JvmStatic
    fun broadcastMessage(message: String) {
        broadcastMessage(Component.literal(message))
    }

    /**
     * 向所有在线玩家广播消息组件
     * @param message 消息组件
     */
    @JvmStatic
    fun broadcastMessage(message: Component) {
        for (player in onlinePlayers) {
            player.sendSystemMessage(message)
        }
    }

    /**
     * 按名称获取在线玩家
     * @param name 玩家名
     * @return 玩家实例，不在线则返回 null
     */
    @JvmStatic
    fun getPlayer(name: String): ServerPlayer? {
        return server.playerList.getPlayerByName(name)
    }

    /**
     * 按 UUID 获取在线玩家
     * @param uuid 玩家 UUID
     * @return 玩家实例，不在线则返回 null
     */
    @JvmStatic
    fun getPlayer(uuid: UUID): ServerPlayer? {
        return server.playerList.getPlayer(uuid)
    }

    /**
     * 按维度名称获取世界
     * @param dimension 维度标识符（如 "overworld"、"the_nether"、"the_end"）
     * @return 对应的 ServerLevel，不存在则返回 null
     */
    @JvmStatic
    fun getLevel(dimension: String): ServerLevel? {
        val key = ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace(dimension))
        return server.getLevel(key)
    }

    /**
     * 获取所有已加载的世界
     * @return ServerLevel 列表
     */
    @JvmStatic
    fun allLevels(): List<ServerLevel> {
        return server.allLevels.toList()
    }
}
