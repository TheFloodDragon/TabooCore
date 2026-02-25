package taboocore.proxy

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.Unit as McUnit
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.ItemEnchantments

/**
 * 物品代理，提供链式 API 操作 ItemStack
 *
 * 使用示例：
 * ```kotlin
 * val item = item(Items.DIAMOND_SWORD) {
 *     name("§6神剑")
 *     lore("§7传说中的武器", "§7攻击力 +100")
 *     enchant(Enchantments.SHARPNESS, 5)
 *     unbreakable(true)
 *     amount(1)
 * }
 * ```
 */
class ItemProxy(val handle: ItemStack) {

    /** 物品自定义名称（纯文本），读取/设置 CUSTOM_NAME 组件 */
    var name: String
        get() = handle.get(DataComponents.CUSTOM_NAME)?.string ?: handle.hoverName.string
        set(value) {
            handle.set(DataComponents.CUSTOM_NAME, Component.literal(value))
        }

    /** 物品自定义显示名称（Component 类型） */
    var displayName: Component
        get() = handle.get(DataComponents.CUSTOM_NAME) ?: handle.hoverName
        set(value) {
            handle.set(DataComponents.CUSTOM_NAME, value)
        }

    /** 物品描述（Lore），以 Component 列表形式读写 */
    var lore: List<Component>
        get() = handle.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines()
        set(value) {
            handle.set(DataComponents.LORE, ItemLore(value))
        }

    /** 物品数量 */
    var amount: Int
        get() = handle.count
        set(value) {
            handle.count = value
        }

    /** 物品损伤值 */
    var damage: Int
        get() = handle.damageValue
        set(value) {
            handle.damageValue = value
        }

    /** 自定义模型数据（第一个 float 值，向后兼容） */
    var customModelData: Int
        get() {
            val cmd = handle.get(DataComponents.CUSTOM_MODEL_DATA) ?: return 0
            val floats = cmd.floats()
            return if (floats.isNotEmpty()) floats[0].toInt() else 0
        }
        set(value) {
            handle.set(
                DataComponents.CUSTOM_MODEL_DATA,
                CustomModelData(listOf(value.toFloat()), listOf(), listOf(), listOf())
            )
        }

    /** 是否不可破坏 */
    var isUnbreakable: Boolean
        get() = handle.has(DataComponents.UNBREAKABLE)
        set(value) {
            if (value) {
                handle.set(DataComponents.UNBREAKABLE, McUnit.INSTANCE)
            } else {
                handle.remove(DataComponents.UNBREAKABLE)
            }
        }

    // ======================== 附魔 ========================

    /**
     * 添加附魔（如果已有则升级到更高等级）
     * @param enchantment 附魔类型
     * @param level 附魔等级
     * @return 当前代理（链式调用）
     */
    fun enchant(enchantment: Holder<Enchantment>, level: Int): ItemProxy {
        handle.enchant(enchantment, level)
        return this
    }

    /**
     * 移除指定附魔
     * @param enchantment 要移除的附魔类型
     * @return 当前代理（链式调用）
     */
    fun removeEnchantment(enchantment: Holder<Enchantment>): ItemProxy {
        EnchantmentHelper.updateEnchantments(handle) { mutable ->
            mutable.set(enchantment, 0)
        }
        return this
    }

    /** 获取所有附魔及其等级 */
    val enchantments: Map<Holder<Enchantment>, Int>
        get() {
            val result = mutableMapOf<Holder<Enchantment>, Int>()
            for (entry in handle.enchantments.entrySet()) {
                result[entry.key] = entry.intValue
            }
            return result
        }

    // ======================== 属性修饰符 ========================

    /**
     * 添加属性修饰符
     * @param attribute 属性类型
     * @param modifier 修饰符
     * @param slot 装备槽组
     * @return 当前代理（链式调用）
     */
    fun addAttributeModifier(
        attribute: Holder<Attribute>,
        modifier: AttributeModifier,
        slot: EquipmentSlotGroup
    ): ItemProxy {
        val current = handle.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
        handle.set(DataComponents.ATTRIBUTE_MODIFIERS, current.withModifierAdded(attribute, modifier, slot))
        return this
    }

    // ======================== DataComponents 通用访问 ========================

    /**
     * 获取指定类型的数据组件
     * @param type 组件类型
     * @return 组件值，不存在则返回 null
     */
    fun <T : Any> getComponent(type: DataComponentType<T>): T? {
        return handle.get(type)
    }

    /**
     * 设置指定类型的数据组件
     * @param type 组件类型
     * @param value 组件值
     * @return 当前代理（链式调用）
     */
    fun <T : Any> setComponent(type: DataComponentType<T>, value: T): ItemProxy {
        handle.set(type, value)
        return this
    }

    /**
     * 移除指定类型的数据组件
     * @param type 组件类型
     * @return 当前代理（链式调用）
     */
    fun removeComponent(type: DataComponentType<*>): ItemProxy {
        handle.remove(type)
        return this
    }

    /**
     * 返回底层 ItemStack
     */
    fun build(): ItemStack = handle
}

/**
 * 创建物品代理并通过构建器配置
 * @param type 物品类型
 * @param count 数量，默认 1
 * @param block 配置代码块
 * @return 配置完成的 ItemStack
 */
fun item(type: Item, count: Int = 1, block: ItemProxy.() -> Unit = {}): ItemStack {
    val stack = ItemStack(type, count)
    ItemProxy(stack).apply(block)
    return stack
}

/**
 * 将 ItemStack 包装为 ItemProxy
 */
fun ItemStack.proxy(): ItemProxy = ItemProxy(this)

/**
 * 在代理上编辑 ItemStack 并返回
 * @param block 编辑代码块
 * @return 编辑后的 ItemStack
 */
fun ItemStack.edit(block: ItemProxy.() -> Unit): ItemStack {
    ItemProxy(this).apply(block)
    return this
}
