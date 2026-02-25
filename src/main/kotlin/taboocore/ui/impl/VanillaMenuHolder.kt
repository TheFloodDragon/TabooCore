package taboocore.ui.impl

import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

/**
 * 虚拟容器菜单，作为 UI 的 NMS 后端
 *
 * 基于原版 [ChestMenu]，覆写 stillValid 始终返回 true（不绑定物理方块），
 * 并禁止 quickMoveStack 以防止物品被 Shift-Click 移出虚拟菜单。
 */
class VanillaMenuHolder(
    menuType: MenuType<ChestMenu>,
    containerId: Int,
    playerInventory: Inventory,
    container: SimpleContainer,
    rows: Int
) : ChestMenu(menuType, containerId, playerInventory, container, rows) {

    /**
     * 始终返回 true，因为虚拟菜单不绑定到物理方块
     */
    override fun stillValid(player: Player): Boolean = true

    /**
     * 禁止 Shift-Click 移动物品，返回空堆
     */
    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack = ItemStack.EMPTY
}
