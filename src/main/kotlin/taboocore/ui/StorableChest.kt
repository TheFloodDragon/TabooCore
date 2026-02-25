package taboocore.ui

import net.minecraft.world.item.ItemStack

/**
 * 可储存容器接口
 * 与 TabooLib bukkit-ui 的 StorableChest 接口保持一致
 */
interface StorableChest : Chest {

    /**
     * 定义页面规则
     */
    fun rule(rule: Rule.() -> Unit)

    /**
     * 页面规则
     */
    interface Rule {

        /**
         * 定义判定位置（单个槽位）
         * 玩家是否可以将物品放入
         */
        fun checkSlot(slot: Int, check: (ItemStack) -> Boolean)

        /**
         * 定义判定位置（槽位范围）
         * 玩家是否可以将物品放入
         */
        fun checkSlot(range: IntRange, check: (ItemStack, Int) -> Boolean)

        /**
         * 获取页面中首个有效位置
         * 用于玩家 SHIFT 点击快速放入物品
         */
        fun firstSlot(provider: (ItemStack) -> Int)

        /**
         * 物品写入回调
         */
        fun writeItem(writer: (slot: Int, ItemStack) -> Unit)

        /**
         * 读取物品回调
         */
        fun readItem(reader: (slot: Int) -> ItemStack?)
    }
}
