package taboocore.chat

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.net.URI

/**
 * 富文本组件，封装 NMS Component 提供链式 API
 *
 * 使用示例：
 * ```kotlin
 * val text = text("Hello ") + bold("World") + colored("!", ChatColors.RED)
 * val hover = text("Click me!").hover("Execute /help").click(ClickAction.RUN_COMMAND, "/help")
 * player.sendSystemMessage(hover.toComponent())
 * ```
 */
class ComponentText private constructor(private var root: MutableComponent) {

    constructor(text: String) : this(Component.literal(text))
    constructor(component: Component) : this(component.copy())

    // ========================
    // 格式化
    // ========================

    /** 设置粗体 */
    fun bold(bold: Boolean = true): ComponentText {
        root = root.withStyle { it.withBold(bold) }
        return this
    }

    /** 设置斜体 */
    fun italic(italic: Boolean = true): ComponentText {
        root = root.withStyle { it.withItalic(italic) }
        return this
    }

    /** 设置下划线 */
    fun underlined(underlined: Boolean = true): ComponentText {
        root = root.withStyle { it.withUnderlined(underlined) }
        return this
    }

    /** 设置删除线 */
    fun strikethrough(strikethrough: Boolean = true): ComponentText {
        root = root.withStyle { it.withStrikethrough(strikethrough) }
        return this
    }

    /** 设置混淆 */
    fun obfuscated(obfuscated: Boolean = true): ComponentText {
        root = root.withStyle { it.withObfuscated(obfuscated) }
        return this
    }

    /** 通过 TextColor 设置颜色 */
    fun color(color: TextColor): ComponentText {
        root = root.withStyle { it.withColor(color) }
        return this
    }

    /** 通过十六进制字符串设置颜色，如 "#FF5555" */
    fun color(hex: String): ComponentText {
        val rgb = Integer.parseInt(hex.removePrefix("#"), 16)
        root = root.withStyle { it.withColor(rgb) }
        return this
    }

    // ========================
    // 交互事件
    // ========================

    /** 设置悬浮文本（纯文本） */
    fun hover(text: String): ComponentText {
        root = root.withStyle { it.withHoverEvent(HoverEvent.ShowText(Component.literal(text))) }
        return this
    }

    /** 设置悬浮文本（Component） */
    fun hover(component: Component): ComponentText {
        root = root.withStyle { it.withHoverEvent(HoverEvent.ShowText(component)) }
        return this
    }

    /** 设置点击事件 */
    fun click(action: ClickAction, value: String): ComponentText {
        val event: ClickEvent = when (action) {
            ClickAction.OPEN_URL -> ClickEvent.OpenUrl(URI.create(value))
            ClickAction.OPEN_FILE -> ClickEvent.OpenFile(value)
            ClickAction.RUN_COMMAND -> ClickEvent.RunCommand(value)
            ClickAction.SUGGEST_COMMAND -> ClickEvent.SuggestCommand(value)
            ClickAction.CHANGE_PAGE -> ClickEvent.ChangePage(value.toInt())
            ClickAction.COPY_TO_CLIPBOARD -> ClickEvent.CopyToClipboard(value)
        }
        root = root.withStyle { it.withClickEvent(event) }
        return this
    }

    /** 设置插入文本（Shift+点击插入聊天框） */
    fun insertion(text: String): ComponentText {
        root = root.withStyle { it.withInsertion(text) }
        return this
    }

    // ========================
    // 组合
    // ========================

    /** 追加另一个 ComponentText */
    operator fun plus(other: ComponentText): ComponentText {
        root.append(other.root)
        return this
    }

    /** 追加纯文本 */
    operator fun plus(other: String): ComponentText {
        root.append(other)
        return this
    }

    /** 追加 NMS Component */
    operator fun plus(other: Component): ComponentText {
        root.append(other)
        return this
    }

    // ========================
    // 转换
    // ========================

    /** 转换为 NMS Component */
    fun toComponent(): MutableComponent = root

    /** 转换为旧版 section-code 格式字符串 */
    fun toLegacy(): String = ComponentSerializer.toLegacy(root)

    /** 转换为 JSON 字符串 */
    fun toJson(): String = ComponentSerializer.toJson(root)

    override fun toString(): String = root.string

    companion object {
        /** 从 JSON 反序列化 */
        fun fromJson(json: String): ComponentText = ComponentText(ComponentSerializer.fromJson(json))

        /** 从旧版 section-code 字符串创建 */
        fun fromLegacy(text: String): ComponentText = ComponentText(ComponentSerializer.fromLegacy(text))

        /** 创建空的 ComponentText */
        fun empty(): ComponentText = ComponentText(Component.empty())
    }
}

/**
 * 点击事件类型
 */
enum class ClickAction {
    OPEN_URL,
    OPEN_FILE,
    RUN_COMMAND,
    SUGGEST_COMMAND,
    CHANGE_PAGE,
    COPY_TO_CLIPBOARD
}

// ========================
// 构建 DSL 函数
// ========================

/** 创建纯文本 ComponentText */
fun text(content: String): ComponentText = ComponentText(content)

/** 从 NMS Component 创建 ComponentText */
fun text(content: Component): ComponentText = ComponentText(content)

/** 创建可翻译文本 */
fun translatable(key: String, vararg args: Any): ComponentText {
    return ComponentText(Component.translatable(key, *args))
}

/** 创建带颜色的文本 */
fun colored(content: String, color: TextColor): ComponentText {
    return ComponentText(content).color(color)
}

/** 创建粗体文本 */
fun bold(content: String): ComponentText {
    return ComponentText(content).bold()
}

/** 创建斜体文本 */
fun italic(content: String): ComponentText {
    return ComponentText(content).italic()
}
