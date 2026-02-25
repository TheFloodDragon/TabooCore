package taboocore.chat

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

/**
 * Component 序列化/反序列化工具
 */
object ComponentSerializer {

    /** 将 NMS Component 序列化为 JSON 字符串 */
    fun toJson(component: Component): String {
        val jsonElement = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component)
        return jsonElement.result().map { it.toString() }.orElse("{}")
    }

    /** 从 JSON 字符串反序列化为 NMS Component */
    fun fromJson(json: String): MutableComponent {
        val jsonElement = JsonParser.parseString(json)
        val result = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement)
        return (result.result().orElse(Component.empty()) as Component).copy()
    }

    /**
     * 将 section-code 格式字符串（如 "§aHello §bWorld"）转为 NMS Component
     */
    fun fromLegacy(text: String): MutableComponent {
        val root = Component.empty()
        var current = StringBuilder()
        var currentStyle = Style.EMPTY
        var i = 0
        while (i < text.length) {
            if (text[i] == '\u00a7' && i + 1 < text.length) {
                // 先输出已累积的文本
                if (current.isNotEmpty()) {
                    root.append(Component.literal(current.toString()).withStyle(currentStyle))
                    current = StringBuilder()
                }
                val code = text[i + 1]
                val formatting = ChatFormatting.getByCode(code)
                if (formatting != null) {
                    currentStyle = if (formatting == ChatFormatting.RESET) {
                        Style.EMPTY
                    } else if (formatting.isColor) {
                        // 颜色代码会重置所有格式
                        Style.EMPTY.withColor(TextColor.fromLegacyFormat(formatting))
                    } else {
                        currentStyle.applyFormat(formatting)
                    }
                } else if (code == 'x' || code == 'X') {
                    // 尝试解析 hex 颜色 §x§r§r§g§g§b§b
                    if (i + 13 < text.length) {
                        val hex = StringBuilder("#")
                        var valid = true
                        for (j in 0 until 6) {
                            val idx = i + 2 + j * 2
                            if (idx + 1 < text.length && text[idx] == '\u00a7') {
                                hex.append(text[idx + 1])
                            } else {
                                valid = false
                                break
                            }
                        }
                        if (valid) {
                            val rgb = hex.toString().removePrefix("#").toIntOrNull(16)
                            if (rgb != null) {
                                currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(rgb))
                                i += 14
                                continue
                            }
                        }
                    }
                }
                i += 2
            } else {
                current.append(text[i])
                i++
            }
        }
        // 输出最后的文本
        if (current.isNotEmpty()) {
            root.append(Component.literal(current.toString()).withStyle(currentStyle))
        }
        return root
    }

    /**
     * 将 NMS Component 转为 section-code 格式字符串
     */
    fun toLegacy(component: Component): String {
        val sb = StringBuilder()
        component.visit({ style, text ->
            val color = style.color
            if (color != null) {
                val formatting = ChatFormatting.entries.firstOrNull {
                    it.isColor && it.color != null && TextColor.fromLegacyFormat(it) == color
                }
                if (formatting != null) {
                    sb.append(formatting.toString())
                }
                // 非标准颜色无法用 section-code 精确表示，略过
            }
            if (style.isBold) sb.append(ChatFormatting.BOLD.toString())
            if (style.isItalic) sb.append(ChatFormatting.ITALIC.toString())
            if (style.isUnderlined) sb.append(ChatFormatting.UNDERLINE.toString())
            if (style.isStrikethrough) sb.append(ChatFormatting.STRIKETHROUGH.toString())
            if (style.isObfuscated) sb.append(ChatFormatting.OBFUSCATED.toString())
            sb.append(text)
            java.util.Optional.empty<Unit>()
        }, Style.EMPTY)
        return sb.toString()
    }
}
