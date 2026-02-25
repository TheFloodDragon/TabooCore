package taboocore.ui.impl

import net.minecraft.world.item.ItemStack
import taboocore.ui.StorableChest

/**
 * 可储存容器实现
 * 继承 [ChestImpl]，允许特定槽位接受玩家放入物品
 */
open class StorableChestImpl(title: String) : ChestImpl(title), StorableChest {

    /** 规则实例 */
    val storableRule = RuleImpl()

    init {
        // 默认不锁定主手，允许玩家操作物品
        handLocked = false
    }

    override fun rule(rule: StorableChest.Rule.() -> Unit) {
        storableRule.apply(rule)
    }

    /**
     * 规则实现
     */
    class RuleImpl : StorableChest.Rule {

        /** 单槽位检查 */
        val slotChecks = mutableMapOf<Int, (ItemStack) -> Boolean>()

        /** 范围槽位检查 */
        val rangeChecks = mutableListOf<Pair<IntRange, (ItemStack, Int) -> Boolean>>()

        /** 首个有效位置查找器 */
        var firstSlotProvider: ((ItemStack) -> Int)? = null

        /** 物品写入回调 */
        var writeCallback: ((slot: Int, ItemStack) -> Unit)? = null

        /** 物品读取回调 */
        var readCallback: ((slot: Int) -> ItemStack?)? = null

        override fun checkSlot(slot: Int, check: (ItemStack) -> Boolean) {
            slotChecks[slot] = check
        }

        override fun checkSlot(range: IntRange, check: (ItemStack, Int) -> Boolean) {
            rangeChecks += range to check
        }

        override fun firstSlot(provider: (ItemStack) -> Int) {
            firstSlotProvider = provider
        }

        override fun writeItem(writer: (slot: Int, ItemStack) -> Unit) {
            writeCallback = writer
        }

        override fun readItem(reader: (slot: Int) -> ItemStack?) {
            readCallback = reader
        }

        /**
         * 检查指定槽位是否允许放入物品
         */
        fun canPlace(slot: Int, item: ItemStack): Boolean {
            // 检查单槽位规则
            slotChecks[slot]?.let { return it(item) }
            // 检查范围规则
            for ((range, check) in rangeChecks) {
                if (slot in range) return check(item, slot)
            }
            return false
        }
    }
}
