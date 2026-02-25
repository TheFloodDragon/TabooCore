package taboocore.ui

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack

/**
 * UI 点击事件
 * 与 TabooLib bukkit-ui 的 ClickEvent 保持一致的 API
 * 同时包含原 ClickContext 的所有信息
 *
 * @property clicker 点击的玩家
 * @property rawSlot 原始槽位索引
 * @property slot 抽象字符（由 map 布局映射）
 * @property clickType 点击类型（LEFT/RIGHT/SHIFT_LEFT 等详细分类）
 * @property currentItem 被点击槽位上的物品
 * @property cursorItem 光标上持有的物品
 * @property builder 所属的 Chest 菜单
 * @property containerInput NMS 容器输入类型（原始值）
 * @property buttonNum 点击的按钮编号（原始值）
 */
class ClickEvent(
    val clicker: ServerPlayer,
    val rawSlot: Int,
    val slot: Char,
    val clickType: ClickType,
    val currentItem: ItemStack,
    var cursorItem: ItemStack,
    val builder: Chest,
    val containerInput: ContainerInput = ContainerInput.PICKUP,
    val buttonNum: Int = 0
) {

    /** 是否取消事件（取消后阻止原版物品移动） */
    var isCancelled: Boolean = false

    // ======================== 便捷属性 ========================

    /** 点击的玩家（与 clicker 相同，兼容旧命名） */
    val player: ServerPlayer get() = clicker

    /** 是否左键点击 */
    val isLeftClick: Boolean get() = clickType == ClickType.LEFT

    /** 是否右键点击 */
    val isRightClick: Boolean get() = clickType == ClickType.RIGHT

    /** 是否 Shift + 左键 */
    val isShiftLeftClick: Boolean get() = clickType == ClickType.SHIFT_LEFT

    /** 是否 Shift + 右键 */
    val isShiftRightClick: Boolean get() = clickType == ClickType.SHIFT_RIGHT

    /** 是否 Shift 点击（左或右） */
    val isShiftClick: Boolean get() = clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT

    /** 是否中键点击 */
    val isMiddleClick: Boolean get() = clickType == ClickType.MIDDLE

    /** 是否数字键操作 */
    val isNumberKey: Boolean get() = clickType == ClickType.NUMBER_KEY

    /** 是否 Q 键丢弃 */
    val isDrop: Boolean get() = clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP

    /** 是否 F 键切换副手 */
    val isOffhandSwap: Boolean get() = clickType == ClickType.SWAP_OFFHAND

    /** 是否拖拽操作 */
    val isDrag: Boolean get() = clickType == ClickType.DRAG

    /** 是否双击收集 */
    val isDoubleClick: Boolean get() = clickType == ClickType.DOUBLE_CLICK

    /** 数字键对应的热栏位（仅 NUMBER_KEY 时有效，否则为 -1） */
    val hotbarKey: Int
        get() = if (clickType == ClickType.NUMBER_KEY) buttonNum else -1

    // ======================== 物品访问 ========================

    /**
     * 获取指定抽象字符对应的物品
     * 返回该字符首个槽位上的物品
     *
     * @param slot 抽象字符
     * @return 物品，如果不存在则返回 null
     */
    fun getItem(slot: Char): ItemStack? {
        return builder.items[slot]
    }

    /**
     * 获取指定抽象字符对应的所有物品
     *
     * @param slot 抽象字符
     * @return 物品列表
     */
    fun getItems(slot: Char): List<ItemStack> {
        return builder.getSlots(slot).mapNotNull { builder.items[builder.getSlot(it)] }
    }
}

