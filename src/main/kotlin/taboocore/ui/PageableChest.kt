package taboocore.ui

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * 可翻页容器接口
 * 与 TabooLib bukkit-ui 的 PageableChest 接口保持一致
 */
interface PageableChest<T> : Chest {

    /** 当前页数 */
    val page: Int

    /**
     * 设置页数
     */
    fun page(page: Int)

    /**
     * 设置可用位置
     */
    fun slots(slots: List<Int>)

    /**
     * 通过抽象字符选择由 map 函数铺设的页面位置
     */
    fun slotsBy(char: Char)

    /**
     * 可用元素列表回调
     */
    fun elements(elements: () -> List<T>)

    /**
     * 元素对应物品生成回调
     */
    fun onGenerate(async: Boolean = false, callback: (player: ServerPlayer, element: T, index: Int, slot: Int) -> ItemStack)

    /**
     * 元素点击回调
     */
    fun onClick(callback: (event: ClickEvent, element: T) -> Unit)

    /**
     * 设置下一页按钮
     *
     * @param slot 按钮位置
     * @param roll 是否循环翻页
     * @param callback 按钮物品回调
     */
    fun setNextPage(slot: Int, roll: Boolean = false, callback: (page: Int, hasNextPage: Boolean) -> ItemStack)

    /**
     * 设置上一页按钮
     *
     * @param slot 按钮位置
     * @param roll 是否循环翻页
     * @param callback 按钮物品回调
     */
    fun setPreviousPage(slot: Int, roll: Boolean = false, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack)

    /**
     * 切换页面回调
     */
    fun onPageChange(callback: (player: ServerPlayer) -> Unit)

    /**
     * 是否可以返回上一页
     */
    fun hasPreviousPage(): Boolean

    /**
     * 是否可以前往下一页
     */
    fun hasNextPage(): Boolean

    /**
     * 重置元素列表缓存
     */
    fun resetElementsCache()
}
