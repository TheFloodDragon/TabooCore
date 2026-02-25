package taboocore.data

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.component.CustomData

/**
 * 实体数据工具，操作实体的持久化数据
 *
 * 使用示例：
 * ```kotlin
 * entity.editPersistentData {
 *     putString("myKey", "myValue")
 *     putInt("level", 5)
 * }
 * val value = entity.getPersistentTag("myKey")
 * ```
 */

// ========================
// 持久化数据（基于 Entity 的 CustomData 组件）
// ========================

/**
 * 获取实体的持久化 NBT 数据（CustomData 组件）
 * 返回数据的拷贝，修改不会影响原始数据
 */
fun Entity.getPersistentData(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA) ?: return CompoundTag()
    return customData.copyTag()
}

/**
 * 编辑实体的持久化 NBT 数据
 */
fun Entity.editPersistentData(block: CompoundTag.() -> Unit) {
    val tag = getPersistentData()
    block(tag)
    this.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(tag))
}

/**
 * 读取持久化数据中指定键的值
 */
fun Entity.getPersistentTag(key: String): Any? {
    val tag = getPersistentData()
    return if (tag.contains(key)) tag.get(key) else null
}

/**
 * 写入持久化数据
 */
fun Entity.setPersistentTag(key: String, value: Any) {
    editPersistentData {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putDouble(key, value)
            is Boolean -> putBoolean(key, value)
            is ByteArray -> putByteArray(key, value)
            is IntArray -> putIntArray(key, value)
            is LongArray -> putLongArray(key, value)
            is CompoundTag -> put(key, value)
        }
    }
}

// ========================
// 实体标志快捷属性
// ========================
// 注：Entity 本身已提供 isSprinting, isSwimming, isInvisible, isOnFire,
// isCrouching(isShiftKeyDown), setGlowingTag, isCurrentlyGlowing, isFallFlying 等方法。
// 以下仅提供额外的便捷封装。

/**
 * 点燃实体指定时间（tick）
 */
fun Entity.ignite(ticks: Int = 200) {
    this.remainingFireTicks = ticks
}

/**
 * 设置发光状态
 */
var Entity.glowing: Boolean
    get() = this.isCurrentlyGlowing
    set(value) {
        this.setGlowingTag(value)
    }
