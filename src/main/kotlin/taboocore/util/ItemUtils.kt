package taboocore.util

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * 物品工具
 * 提供 ItemStack 的常用操作和 DSL 构建方式
 */
object ItemUtils {

    /**
     * 深拷贝物品
     * @return 物品的完整副本
     */
    @JvmStatic
    fun ItemStack.deepCopy(): ItemStack {
        return this.copy()
    }

    /**
     * 比较两个物品是否相似（忽略数量）
     * @param other 要比较的物品
     * @return 类型和数据是否一致
     */
    @JvmStatic
    fun ItemStack.isSimilar(other: ItemStack): Boolean {
        if (this.isEmpty && other.isEmpty) return true
        if (this.item != other.item) return false
        return ItemStack.isSameItemSameComponents(this, other)
    }

    /**
     * 设置物品的显示名称
     * @param name 名称组件
     * @return 当前物品（链式调用）
     */
    @JvmStatic
    fun ItemStack.setName(name: Component): ItemStack {
        this.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, name)
        return this
    }

    /**
     * 设置物品数量
     * @param amount 数量
     * @return 当前物品（链式调用）
     */
    @JvmStatic
    fun ItemStack.setAmount(amount: Int): ItemStack {
        this.count = amount
        return this
    }

    /**
     * 判断物品是否为空
     * @return 是否为空物品
     */
    @JvmStatic
    fun ItemStack.isEmptyStack(): Boolean {
        return this.isEmpty
    }

    /**
     * DSL 方式构建物品
     * @param item 物品类型
     * @param count 数量，默认 1
     * @param builder 构建器操作
     * @return 构建完成的物品
     */
    @JvmStatic
    fun itemStack(item: Item, count: Int = 1, builder: ItemStack.() -> Unit = {}): ItemStack {
        val stack = ItemStack(item, count)
        stack.builder()
        return stack
    }
}
