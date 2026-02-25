package taboocore.util

import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 数学与几何工具
 * 提供插值、限制、随机数和 Vec3 运算等常用操作
 */
object MathUtils {

    /**
     * 线性插值
     * @param start 起始值
     * @param end 结束值
     * @param t 插值因子（0.0 ~ 1.0）
     * @return 插值结果
     */
    @JvmStatic
    fun lerp(start: Double, end: Double, t: Double): Double {
        return start + (end - start) * t
    }

    /**
     * 将值限制在指定范围内
     * @param value 输入值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    @JvmStatic
    fun clamp(value: Double, min: Double, max: Double): Double {
        return max(min, min(max, value))
    }

    /**
     * 生成指定范围内的随机浮点数
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    @JvmStatic
    fun randomBetween(min: Double, max: Double): Double {
        return min + Math.random() * (max - min)
    }

    /**
     * Vec3 加法
     * @param other 另一个向量
     * @return 相加后的新向量
     */
    @JvmStatic
    fun Vec3.plus(other: Vec3): Vec3 {
        return this.add(other)
    }

    /**
     * Vec3 缩放
     * @param factor 缩放因子
     * @return 缩放后的新向量
     */
    @JvmStatic
    fun Vec3.scaled(factor: Double): Vec3 {
        return this.scale(factor)
    }

    /**
     * Vec3 归一化
     * @return 单位向量
     */
    @JvmStatic
    fun Vec3.normalized(): Vec3 {
        return this.normalize()
    }

    /**
     * 计算 Vec3 长度
     * @return 向量长度
     */
    @JvmStatic
    fun Vec3.len(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * 计算两个 Vec3 之间的距离
     * @param other 另一个向量
     * @return 欧氏距离
     */
    @JvmStatic
    fun Vec3.dist(other: Vec3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
