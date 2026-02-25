package taboocore.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/**
 * NBT 操作工具
 * 封装 CompoundTag 的常用操作
 *
 * 注意：现代 Minecraft（1.20.5+）使用 DataComponents 系统，
 * 物品 NBT 数据存储在 CUSTOM_DATA 组件中。
 */
object NbtUtils {

    /**
     * 获取字符串值，不存在时返回默认值
     * @param key 键名
     * @param default 默认值
     * @return 字符串值
     */
    @JvmStatic
    fun CompoundTag.getStringOrDefault(key: String, default: String = ""): String {
        return if (contains(key)) getString(key) as String else default
    }

    /**
     * 获取整数值，不存在时返回默认值
     * @param key 键名
     * @param default 默认值
     * @return 整数值
     */
    @JvmStatic
    fun CompoundTag.getIntOrDefault(key: String, default: Int = 0): Int {
        return if (contains(key)) getInt(key) as Int else default
    }

    /**
     * 获取布尔值，不存在时返回默认值
     * @param key 键名
     * @param default 默认值
     * @return 布尔值
     */
    @JvmStatic
    fun CompoundTag.getBooleanOrDefault(key: String, default: Boolean = false): Boolean {
        return if (contains(key)) getBoolean(key) as Boolean else default
    }

    /**
     * 自动检测类型并存入 NBT
     * @param key 键名
     * @param value 要存入的值，支持 String/Int/Long/Float/Double/Boolean/Byte/Short
     */
    @JvmStatic
    fun CompoundTag.putSafe(key: String, value: Any) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putDouble(key, value)
            is Boolean -> putBoolean(key, value)
            is Byte -> putByte(key, value)
            is Short -> putShort(key, value)
            is CompoundTag -> put(key, value)
            else -> putString(key, value.toString())
        }
    }

    /**
     * 编辑物品的自定义数据 NBT（DataComponents 方式）
     * @param block NBT 编辑操作
     * @return 修改后的物品
     */
    @JvmStatic
    fun ItemStack.editNbt(block: CompoundTag.() -> Unit): ItemStack {
        CustomData.update(DataComponents.CUSTOM_DATA, this) { tag -> block(tag) }
        return this
    }

    /**
     * 获取物品的自定义数据 NBT
     * @return CompoundTag，无自定义数据则返回 null
     */
    @JvmStatic
    fun ItemStack.getNbt(): CompoundTag? {
        val customData = this.get(DataComponents.CUSTOM_DATA) ?: return null
        return customData.copyTag()
    }
}
