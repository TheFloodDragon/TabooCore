package taboocore.data

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.util.Unit as McUnit
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.enchantment.ItemEnchantments

/**
 * DataComponent 工具类，提供 ItemStack 的数据组件便捷操作
 *
 * 使用示例：
 * ```kotlin
 * val item = ItemStack(Items.DIAMOND_SWORD)
 * item.set(DataComponents.DAMAGE, 10)
 * val dmg = item.get(DataComponents.DAMAGE)
 * item.customName = Component.literal("My Sword")
 * item.lore = listOf(Component.literal("A fine sword"))
 * item.unbreakable = true
 * ```
 */

// ========================
// ItemStack 数据组件便捷函数
// ========================
// 注：ItemStack 本身已有 get/set/remove/has 方法，可直接使用：
//   item.get(DataComponents.DAMAGE)
//   item.set(DataComponents.DAMAGE, 10)
//   item.remove(DataComponents.DAMAGE)
//   item.has(DataComponents.DAMAGE)

/** 移除数据组件 */
fun ItemStack.removeComponent(type: DataComponentType<*>) {
    this.remove(type)
}

/** 判断是否存在数据组件 */
fun ItemStack.hasComponent(type: DataComponentType<*>): Boolean {
    return this.has(type)
}

// ========================
// 自定义 NBT 数据操作
// ========================

/** 编辑物品的自定义 NBT 数据 */
fun ItemStack.customData(block: CompoundTag.() -> Unit): ItemStack {
    CustomData.update(DataComponents.CUSTOM_DATA, this) { tag -> block(tag) }
    return this
}

/** 读取自定义 NBT 数据中的指定键值 */
fun ItemStack.getCustomData(key: String): Any? {
    val data = this.get(DataComponents.CUSTOM_DATA) ?: return null
    val tag = data.copyTag()
    return if (tag.contains(key)) tag.get(key) else null
}

/** 写入自定义 NBT 数据 */
fun ItemStack.setCustomData(key: String, value: Any) {
    CustomData.update(DataComponents.CUSTOM_DATA, this) { tag ->
        when (value) {
            is String -> tag.putString(key, value)
            is Int -> tag.putInt(key, value)
            is Long -> tag.putLong(key, value)
            is Float -> tag.putFloat(key, value)
            is Double -> tag.putDouble(key, value)
            is Boolean -> tag.putBoolean(key, value)
            is ByteArray -> tag.putByteArray(key, value)
            is IntArray -> tag.putIntArray(key, value)
            is LongArray -> tag.putLongArray(key, value)
            is CompoundTag -> tag.put(key, value)
        }
    }
}

/** 清除自定义 NBT 数据 */
fun ItemStack.clearCustomData() {
    this.remove(DataComponents.CUSTOM_DATA)
}

// ========================
// 常用组件快捷属性
// ========================
// 注：部分属性名与 ItemStack 原有 getter 冲突，
// 使用不同名称以避免被成员属性遮蔽。

/** 自定义显示名称（可读写，区别于 ItemStack.customName 只读属性） */
var ItemStack.displayName: Component?
    get() = this.get(DataComponents.CUSTOM_NAME)
    set(value) {
        if (value != null) this.set(DataComponents.CUSTOM_NAME, value)
        else this.remove(DataComponents.CUSTOM_NAME)
    }

/** 物品名称组件（可读写，区别于 ItemStack.itemName 只读属性） */
var ItemStack.nameComponent: Component?
    get() = this.get(DataComponents.ITEM_NAME)
    set(value) {
        if (value != null) this.set(DataComponents.ITEM_NAME, value)
        else this.remove(DataComponents.ITEM_NAME)
    }

/** 物品 Lore */
var ItemStack.lore: List<Component>
    get() = (this.get(DataComponents.LORE) ?: ItemLore.EMPTY).lines()
    set(value) {
        this.set(DataComponents.LORE, ItemLore(value))
    }

/** 附魔（可读写，区别于 ItemStack.enchantments 只读属性） */
var ItemStack.enchantmentData: ItemEnchantments
    get() = this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
    set(value) {
        this.set(DataComponents.ENCHANTMENTS, value)
    }

/** 修复费用 */
var ItemStack.repairCost: Int
    get() = this.getOrDefault(DataComponents.REPAIR_COST, 0)
    set(value) {
        this.set(DataComponents.REPAIR_COST, value)
    }

/** 是否不可破坏 */
var ItemStack.unbreakable: Boolean
    get() = this.has(DataComponents.UNBREAKABLE)
    set(value) {
        if (value) this.set(DataComponents.UNBREAKABLE, McUnit.INSTANCE)
        else this.remove(DataComponents.UNBREAKABLE)
    }

/** 自定义模型数据 */
var ItemStack.modelData: CustomModelData?
    get() = this.get(DataComponents.CUSTOM_MODEL_DATA)
    set(value) {
        if (value != null) this.set(DataComponents.CUSTOM_MODEL_DATA, value)
        else this.remove(DataComponents.CUSTOM_MODEL_DATA)
    }

/** 耐久损伤值 */
var ItemStack.damage: Int
    get() = this.getOrDefault(DataComponents.DAMAGE, 0)
    set(value) {
        this.set(DataComponents.DAMAGE, value)
    }

/** 最大耐久值组件（可读写，区别于 ItemStack.maxDamage 只读属性） */
var ItemStack.maxDamageComponent: Int
    get() = this.getOrDefault(DataComponents.MAX_DAMAGE, 0)
    set(value) {
        this.set(DataComponents.MAX_DAMAGE, value)
    }

/** 食物属性 */
var ItemStack.food: FoodProperties?
    get() = this.get(DataComponents.FOOD)
    set(value) {
        if (value != null) this.set(DataComponents.FOOD, value)
        else this.remove(DataComponents.FOOD)
    }

/** 染色颜色 */
var ItemStack.dyeColor: DyeColor?
    get() = this.get(DataComponents.DYE)
    set(value) {
        if (value != null) this.set(DataComponents.DYE, value)
        else this.remove(DataComponents.DYE)
    }

/** 染色物品颜色（皮革护甲等） */
var ItemStack.dyedColor: DyedItemColor?
    get() = this.get(DataComponents.DYED_COLOR)
    set(value) {
        if (value != null) this.set(DataComponents.DYED_COLOR, value)
        else this.remove(DataComponents.DYED_COLOR)
    }

// ========================
// 属性修饰符
// ========================

/**
 * 向物品添加属性修饰符
 */
fun ItemStack.addAttributeModifier(
    attribute: Holder<Attribute>,
    modifier: AttributeModifier,
    slot: EquipmentSlotGroup
): ItemStack {
    val current = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
    val entry = ItemAttributeModifiers.Entry(attribute, modifier, slot)
    val newModifiers = ItemAttributeModifiers(current.modifiers() + entry)
    this.set(DataComponents.ATTRIBUTE_MODIFIERS, newModifiers)
    return this
}
