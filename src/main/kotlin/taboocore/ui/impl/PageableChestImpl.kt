package taboocore.ui.impl

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import taboocore.ui.ClickEvent
import taboocore.ui.PageableChest
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 可翻页容器实现
 * 继承 [ChestImpl]，在构建时根据当前页数和元素列表自动生成物品
 */
open class PageableChestImpl<T>(title: String) : ChestImpl(title), PageableChest<T> {

    /** 当前页数 */
    override var page = 0

    /** 最大页数 */
    var maxPage = 0

    /** 锁定所有位置 */
    var menuLocked = true

    /** 页面可用位置 */
    val menuSlots = CopyOnWriteArrayList<Int>()

    /** 页面可用元素回调 */
    var elementsCallback: (() -> List<T>) = { emptyList() }

    /** 页面可用元素缓存 */
    var elementsCache = emptyList<T>()

    /** 元素点击回调 */
    var elementClickCallback: ((event: ClickEvent, element: T) -> Unit) = { _, _ -> }

    /** 元素生成回调 */
    var generateCallback: ((player: ServerPlayer, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack.EMPTY }

    /** 页面切换回调 */
    var pageChangeCallback: ((player: ServerPlayer) -> Unit) = { _ -> }

    override fun page(page: Int) {
        this.page = page
    }

    override fun slots(slots: List<Int>) {
        this.menuSlots.clear()
        this.menuSlots += slots
    }

    override fun slotsBy(char: Char) {
        slots(getSlots(char))
    }

    override fun elements(elements: () -> List<T>) {
        elementsCallback = elements
    }

    override fun onGenerate(async: Boolean, callback: (player: ServerPlayer, element: T, index: Int, slot: Int) -> ItemStack) {
        generateCallback = callback
    }

    override fun onClick(callback: (event: ClickEvent, element: T) -> Unit) {
        elementClickCallback = callback
    }

    override fun setNextPage(slot: Int, roll: Boolean, callback: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        set(slot) { callback(page, hasNextPage()) }
        onClick(slot) {
            if (hasNextPage()) {
                page++
                refreshPage()
            } else if (roll) {
                page = 0
                refreshPage()
            }
        }
    }

    override fun setPreviousPage(slot: Int, roll: Boolean, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        set(slot) { callback(page, hasPreviousPage()) }
        onClick(slot) {
            if (hasPreviousPage()) {
                page--
                refreshPage()
            } else if (roll) {
                page = maxPage - 1
                refreshPage()
            }
        }
    }

    override fun onPageChange(callback: (player: ServerPlayer) -> Unit) {
        pageChangeCallback = callback
    }

    override fun hasPreviousPage(): Boolean = page > 0

    override fun hasNextPage(): Boolean {
        return if (menuSlots.isEmpty()) false
        else elementsCache.size.toDouble() / menuSlots.size > page + 1
    }

    override fun resetElementsCache() {
        elementsCache = elementsCallback()
    }

    /**
     * 创建标题（支持 %p 占位符表示页码）
     */
    override fun createTitle(): net.minecraft.network.chat.Component {
        return net.minecraft.network.chat.Component.literal(title.replace("%p", (page + 1).toString()))
    }

    /**
     * 构建页面
     */
    override fun build(): SimpleContainer {
        // 更新元素列表缓存
        elementsCache = elementsCallback()

        // 本次页面所使用的元素
        val elementMap = hashMapOf<Int, T>()
        val startIndex = page * menuSlots.size
        val endIndex = minOf((page + 1) * menuSlots.size, elementsCache.size)
        val elementItems = if (startIndex < elementsCache.size) {
            elementsCache.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        // 计算最大页数
        maxPage = if (menuSlots.isEmpty()) 0 else {
            val total = elementsCache.size
            (total + menuSlots.size - 1) / menuSlots.size
        }

        // 设置最终构建回调：填充元素物品
        onFinalBuild { player, _ ->
            this.viewer = player
            elementItems.forEachIndexed { index, element ->
                val slot = menuSlots.getOrNull(index) ?: return@forEachIndexed
                elementMap[slot] = element
            }
        }

        // 设置自身点击回调
        selfClick {
            if (menuLocked) {
                it.isCancelled = true
            }
            val element = elementMap[it.rawSlot] ?: return@selfClick
            elementClickCallback(it, element)
        }

        // 构建基础页面
        val container = super.build()

        // 生成元素物品并放入容器
        val player = viewer
        if (player != null) {
            elementItems.forEachIndexed { index, element ->
                val slot = menuSlots.getOrNull(index) ?: return@forEachIndexed
                val itemStack = generateCallback(player, element, index, slot)
                if (!itemStack.isEmpty) {
                    container.setItem(slot, itemStack)
                }
            }
        }

        return container
    }

    /**
     * 刷新页面（切页时调用）
     */
    private fun refreshPage() {
        val player = viewer ?: return
        open(player)
        pageChangeCallback(player)
    }
}
