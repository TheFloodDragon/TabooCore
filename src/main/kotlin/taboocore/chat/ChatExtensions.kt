package taboocore.chat

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * 聊天扩展函数，方便向玩家发送富文本
 */

// ========================
// 发送消息给玩家
// ========================

/** 向玩家发送 ComponentText 消息 */
fun ServerPlayer.sendMessage(text: ComponentText) {
    sendSystemMessage(text.toComponent())
}

/** 向玩家发送 ActionBar 消息 */
fun ServerPlayer.sendActionBar(text: ComponentText) {
    connection.send(ClientboundSetActionBarTextPacket(text.toComponent()))
}

/** 向玩家发送标题和副标题 */
fun ServerPlayer.sendTitle(
    title: ComponentText? = null,
    subtitle: ComponentText? = null,
    fadein: Int = 10,
    stay: Int = 70,
    fadeout: Int = 20
) {
    connection.send(ClientboundSetTitlesAnimationPacket(fadein, stay, fadeout))
    title?.let { connection.send(ClientboundSetTitleTextPacket(it.toComponent())) }
    subtitle?.let { connection.send(ClientboundSetSubtitleTextPacket(it.toComponent())) }
}

// ========================
// 广播消息
// ========================

/** 向所有在线玩家广播 ComponentText 消息 */
fun MinecraftServer.broadcastMessage(text: ComponentText) {
    playerList.players.forEach { it.sendSystemMessage(text.toComponent()) }
}

/** 向所有在线玩家广播纯文本消息 */
fun MinecraftServer.broadcastMessage(text: String) {
    val component = Component.literal(text)
    playerList.players.forEach { it.sendSystemMessage(component) }
}

// ========================
// 字符串扩展
// ========================

/** 将 section-code 格式字符串解析为 NMS Component */
fun String.toComponent(): Component = ComponentSerializer.fromLegacy(this)

/** 将 section-code 格式字符串解析为 ComponentText */
fun String.toComponentText(): ComponentText = ComponentText.fromLegacy(this)

/** 将 NMS Component 转为 section-code 格式字符串 */
fun Component.toLegacy(): String = ComponentSerializer.toLegacy(this)

// ========================
// 颜色常量
// ========================

/** 标准 Minecraft 聊天颜色常量 */
object ChatColors {
    val BLACK: TextColor = TextColor.fromLegacyFormat(ChatFormatting.BLACK)!!
    val DARK_BLUE: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_BLUE)!!
    val DARK_GREEN: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN)!!
    val DARK_AQUA: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA)!!
    val DARK_RED: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_RED)!!
    val DARK_PURPLE: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)!!
    val GOLD: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD)!!
    val GRAY: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GRAY)!!
    val DARK_GRAY: TextColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)!!
    val BLUE: TextColor = TextColor.fromLegacyFormat(ChatFormatting.BLUE)!!
    val GREEN: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GREEN)!!
    val AQUA: TextColor = TextColor.fromLegacyFormat(ChatFormatting.AQUA)!!
    val RED: TextColor = TextColor.fromLegacyFormat(ChatFormatting.RED)!!
    val LIGHT_PURPLE: TextColor = TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE)!!
    val YELLOW: TextColor = TextColor.fromLegacyFormat(ChatFormatting.YELLOW)!!
    val WHITE: TextColor = TextColor.fromLegacyFormat(ChatFormatting.WHITE)!!
}
