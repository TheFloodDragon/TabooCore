package taboocore.ui

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import taboocore.ui.impl.ChestImpl

/**
 * 构建一个菜单
 * 与 TabooLib bukkit-ui 的 buildMenu 保持一致的泛型语法
 *
 * @param T 菜单类型（Chest, PageableChest, StorableChest）
 * @param title 菜单标题
 * @param builder DSL 构建代码块
 * @return 菜单实例
 */
inline fun <reified T : Menu> buildMenu(title: String = "Chest", builder: T.() -> Unit): T {
    val type = if (T::class.java.isInterface) Menu.getImplementation(T::class.java) else T::class.java
    @Suppress("UNCHECKED_CAST")
    val instance = type.getDeclaredConstructor(String::class.java).newInstance(title) as T
    instance.apply(builder)
    return instance
}

/**
 * 构建一个菜单并为玩家打开
 *
 * @param T 菜单类型
 * @param title 菜单标题（字符串）
 * @param builder DSL 构建代码块
 */
inline fun <reified T : Menu> ServerPlayer.openMenu(title: String = "Chest", builder: T.() -> Unit) {
    try {
        val menu = buildMenu<T>(title, builder)
        openBuiltMenu(this, menu)
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 构建一个菜单并为玩家打开
 *
 * @param T 菜单类型
 * @param title 菜单标题（Component）
 * @param builder DSL 构建代码块
 */
inline fun <reified T : Menu> ServerPlayer.openMenu(title: Component, builder: T.() -> Unit) {
    openMenu<T>(title.string, builder)
}

/**
 * 打开一个已构建的菜单
 *
 * @param player 玩家
 * @param menu 菜单实例
 */
fun openBuiltMenu(player: ServerPlayer, menu: Menu) {
    when (menu) {
        is ChestImpl -> menu.open(player)
        else -> error("不支持的菜单类型: ${menu.javaClass.name}")
    }
}
