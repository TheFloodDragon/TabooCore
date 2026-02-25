package taboocore.platform

import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import taboolib.common.platform.ProxyCommandSender

/**
 * 将原版 [CommandSourceStack] 包装为 TabooLib 的 [ProxyCommandSender]
 *
 * 用于命令系统中将原版命令源转换为 TabooLib 统一的发送者接口。
 * 如果命令源的实体是 [ServerPlayer]，则可以通过 [origin] 获取到玩家对象。
 *
 * @property source 原版命令源
 */
class TabooCoreCommandSender(val source: CommandSourceStack) : ProxyCommandSender {

    override val origin: Any
        get() = source.entity ?: source

    override val name: String
        get() = source.textName

    override var isOp: Boolean
        get() {
            val player = source.player ?: return true // 控制台默认为 OP
            return source.server.playerList.isOp(player.nameAndId())
        }
        set(_) {}

    override fun isOnline(): Boolean = true

    override fun sendMessage(message: String) {
        source.sendSuccess({ Component.literal(message) }, false)
    }

    override fun performCommand(command: String): Boolean {
        return runCatching {
            source.server.commands.performPrefixedCommand(source, command)
            true
        }.getOrElse { false }
    }

    override fun hasPermission(permission: String): Boolean {
        // 原版没有权限系统，OP 即拥有所有权限
        return isOp
    }
}
