package taboocore.proxy

import net.minecraft.world.Container
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * 容器代理，封装 Container 的常用操作
 *
 * 提供 operator get/set、遍历、查找等便捷方法
 */
class ContainerProxy(val handle: Container) {

    /** 容器大小（槽位数量） */
    val size: Int
        get() = handle.containerSize

    /**
     * 获取指定槽位的物品
     * @param slot 槽位索引
     * @return 该槽位的物品
     */
    operator fun get(slot: Int): ItemStack {
        return handle.getItem(slot)
    }

    /**
     * 设置指定槽位的物品
     * @param slot 槽位索引
     * @param item 要设置的物品
     */
    operator fun set(slot: Int, item: ItemStack) {
        handle.setItem(slot, item)
    }

    /**
     * 容器是否为空
     */
    fun isEmpty(): Boolean {
        return handle.isEmpty
    }

    /**
     * 容器是否已满（所有槽位都有物品且数量达到上限）
     */
    fun isFull(): Boolean {
        for (i in 0 until size) {
            val stack = handle.getItem(i)
            if (stack.isEmpty || stack.count < handle.getMaxStackSize(stack)) {
                return false
            }
        }
        return true
    }

    /**
     * 容器是否包含指定物品
     * @param item 物品类型
     * @return 是否包含
     */
    fun contains(item: Item): Boolean {
        for (i in 0 until size) {
            val stack = handle.getItem(i)
            if (!stack.isEmpty && stack.item == item) {
                return true
            }
        }
        return false
    }

    /**
     * 统计指定物品的总数量
     * @param item 物品类型
     * @return 总数量
     */
    fun countItem(item: Item): Int {
        return handle.countItem(item)
    }

    /**
     * 尝试添加物品到容器
     * @param item 要添加的物品
     * @return 剩余未放入的物品（如果全部放入则返回 EMPTY）
     */
    fun addItem(item: ItemStack): ItemStack {
        var remaining = item.copy()
        for (i in 0 until size) {
            if (remaining.isEmpty) break
            val slotItem = handle.getItem(i)
            if (slotItem.isEmpty) {
                val maxSize = handle.getMaxStackSize(remaining)
                val toPlace = remaining.split(minOf(remaining.count, maxSize))
                handle.setItem(i, toPlace)
            } else if (ItemStack.isSameItemSameComponents(slotItem, remaining)) {
                val maxSize = handle.getMaxStackSize(slotItem)
                val space = maxSize - slotItem.count
                if (space > 0) {
                    val toAdd = minOf(remaining.count, space)
                    slotItem.grow(toAdd)
                    remaining.shrink(toAdd)
                }
            }
        }
        handle.setChanged()
        return remaining
    }

    /**
     * 从指定槽位移除指定数量的物品
     * @param slot 槽位索引
     * @param amount 数量
     * @return 被移除的物品
     */
    fun removeItem(slot: Int, amount: Int): ItemStack {
        return handle.removeItem(slot, amount)
    }

    /**
     * 清空指定槽位
     * @param slot 槽位索引
     */
    fun clearSlot(slot: Int) {
        handle.setItem(slot, ItemStack.EMPTY)
    }

    /**
     * 清空整个容器
     */
    fun clear() {
        handle.clearContent()
    }

    /**
     * 将容器所有物品转为列表
     * @return 物品列表
     */
    fun toList(): List<ItemStack> {
        return (0 until size).map { handle.getItem(it) }
    }

    /**
     * 遍历每个槽位及其物品
     * @param action 回调，参数为槽位索引和物品
     */
    fun forEachSlot(action: (Int, ItemStack) -> Unit) {
        for (i in 0 until size) {
            action(i, handle.getItem(i))
        }
    }

    /**
     * 查找第一个包含指定物品的槽位
     * @param item 物品类型
     * @return 槽位索引，未找到返回 null
     */
    fun findSlot(item: Item): Int? {
        for (i in 0 until size) {
            val stack = handle.getItem(i)
            if (!stack.isEmpty && stack.item == item) {
                return i
            }
        }
        return null
    }

    /**
     * 查找第一个满足条件的槽位
     * @param predicate 判断条件
     * @return 槽位索引，未找到返回 null
     */
    fun findSlot(predicate: (ItemStack) -> Boolean): Int? {
        for (i in 0 until size) {
            if (predicate(handle.getItem(i))) {
                return i
            }
        }
        return null
    }
}

/**
 * 将 Container 包装为 ContainerProxy
 */
fun Container.proxy(): ContainerProxy = ContainerProxy(this)

/**
 * 将 AbstractContainerMenu 包装为 ContainerProxy（操作其所有槽位）
 */
fun AbstractContainerMenu.proxy(): ContainerProxy {
    val menu = this
    return ContainerProxy(object : Container {
        override fun getContainerSize(): Int = menu.slots.size
        override fun isEmpty(): Boolean = menu.slots.all { it.item.isEmpty }
        override fun getItem(slot: Int): ItemStack = menu.slots[slot].item
        override fun removeItem(slot: Int, count: Int): ItemStack = menu.slots[slot].remove(count)
        override fun removeItemNoUpdate(slot: Int): ItemStack {
            val item = menu.slots[slot].item.copy()
            menu.slots[slot].set(ItemStack.EMPTY)
            return item
        }
        override fun setItem(slot: Int, itemStack: ItemStack) {
            menu.slots[slot].set(itemStack)
        }
        override fun setChanged() {}
        override fun stillValid(player: net.minecraft.world.entity.player.Player): Boolean = true
        override fun clearContent() {
            for (i in 0 until menu.slots.size) {
                menu.slots[i].set(ItemStack.EMPTY)
            }
        }
    })
}
