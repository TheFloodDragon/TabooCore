package taboocore.ui

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 标准容器接口
 * 与 TabooLib bukkit-ui 的 Chest 接口保持一致的 API
 */
interface Chest : Menu {

    /** 获取行数 */
    val rows: Int

    /** 物品与对应抽象字符关系 */
    val items: ConcurrentHashMap<Char, ItemStack>

    /** 抽象字符布局 */
    val slots: CopyOnWriteArrayList<List<Char>>

    /** 是否锁定主手 */
    val handLocked: Boolean

    /** 是否打开过 */
    val isOpened: Boolean

    /**
     * 行数
     * 为 1-6 之间的整数
     */
    fun rows(rows: Int)

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品、拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    fun handLocked(handLocked: Boolean)

    /**
     * 页面构建时触发回调
     * 可选是否异步执行
     */
    fun onBuild(async: Boolean = false, callback: (player: ServerPlayer, menu: Chest) -> Unit)

    /**
     * 页面关闭时触发回调
     *
     * @param once 只触发一次
     * @param callback 回调函数
     */
    fun onClose(once: Boolean = true, callback: (player: ServerPlayer, menu: Chest) -> Unit)

    /**
     * 点击事件回调（绑定到指定槽位索引）
     */
    fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 点击事件回调（绑定到指定抽象字符）
     */
    fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 整页点击事件回调
     * 可选是否自动锁定点击位置
     */
    fun onClick(lock: Boolean = false, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 使用抽象字符页面布局
     * 例如 map("AAAAAAAAA", "ABBBBBBA", "AAAAAAAAA")
     */
    fun map(vararg slots: String)

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, itemStack: ItemStack)

    /**
     * 根据位置设置物品
     */
    fun set(slot: Int, itemStack: ItemStack)

    /**
     * 根据抽象符号设置物品（带点击回调）
     */
    fun set(slot: Char, itemStack: ItemStack, onClick: ClickEvent.() -> Unit)

    /**
     * 根据位置设置物品（带点击回调）
     */
    fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit)

    /**
     * 根据抽象符号设置物品（延迟生成）
     */
    fun set(slot: Char, callback: () -> ItemStack)

    /**
     * 根据位置设置物品（延迟生成）
     */
    fun set(slot: Int, callback: () -> ItemStack)

    /**
     * 获取位置对应的抽象字符
     */
    fun getSlot(slot: Int): Char

    /**
     * 获取抽象字符对应的所有位置
     */
    fun getSlots(slot: Char): List<Int>

    /**
     * 获取抽象字符对应的首个位置
     */
    fun getFirstSlot(slot: Char): Int

    /**
     * 更新标题
     */
    fun updateTitle(title: Component)
}
