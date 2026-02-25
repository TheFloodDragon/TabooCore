package taboocore.ui

import net.minecraft.world.inventory.ContainerInput

/**
 * UI 点击类型枚举
 * 与 TabooLib bukkit-ui 的 ClickType 保持一致的 API
 */
enum class ClickType {

    /** 左键单击 */
    LEFT,

    /** Shift + 左键 */
    SHIFT_LEFT,

    /** 右键单击 */
    RIGHT,

    /** Shift + 右键 */
    SHIFT_RIGHT,

    /** 中键（创造模式克隆） */
    MIDDLE,

    /** 数字键（1-9 热栏切换） */
    NUMBER_KEY,

    /** Q 键丢弃单个 */
    DROP,

    /** Ctrl + Q 丢弃整组 */
    CONTROL_DROP,

    /** F 键切换副手 */
    SWAP_OFFHAND,

    /** 双击收集 */
    DOUBLE_CLICK,

    /** 拖拽操作（QUICK_CRAFT） */
    DRAG,

    /** 未知操作 */
    UNKNOWN;

    companion object {

        /**
         * 从 NMS [ContainerInput] 和 buttonNum 推断点击类型
         *
         * @param input NMS 容器输入类型
         * @param buttonNum 点击的按钮编号（0=左键, 1=右键, 40=F 键等）
         * @return 对应的 [ClickType]
         */
        fun from(input: ContainerInput, buttonNum: Int): ClickType = when (input) {
            ContainerInput.PICKUP -> if (buttonNum == 0) LEFT else RIGHT
            ContainerInput.QUICK_MOVE -> if (buttonNum == 0) SHIFT_LEFT else SHIFT_RIGHT
            ContainerInput.SWAP -> when (buttonNum) {
                40 -> SWAP_OFFHAND
                in 0..8 -> NUMBER_KEY
                else -> UNKNOWN
            }

            ContainerInput.CLONE -> MIDDLE
            ContainerInput.THROW -> if (buttonNum == 1) CONTROL_DROP else DROP
            ContainerInput.QUICK_CRAFT -> DRAG
            ContainerInput.PICKUP_ALL -> DOUBLE_CLICK
        }

        /**
         * 判断是否为拖拽操作
         */
        fun isDrag(input: ContainerInput): Boolean = input == ContainerInput.QUICK_CRAFT
    }
}
