package taboocore.util

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

/**
 * 位置与方向工具
 * 提供 BlockPos/Vec3 之间的转换和距离计算等常用操作
 */
object LocationUtils {

    /**
     * 将 BlockPos 转换为 Vec3（取方块中心点）
     * @return 对应的 Vec3
     */
    @JvmStatic
    fun BlockPos.toVec3(): Vec3 {
        return Vec3(x + 0.5, y + 0.5, z + 0.5)
    }

    /**
     * 将 Vec3 转换为 BlockPos（向下取整）
     * @return 对应的 BlockPos
     */
    @JvmStatic
    fun Vec3.toBlockPos(): BlockPos {
        return BlockPos.containing(x, y, z)
    }

    /**
     * 计算两个 BlockPos 之间的距离
     * @param other 另一个位置
     * @return 欧氏距离
     */
    @JvmStatic
    fun BlockPos.distanceTo(other: BlockPos): Double {
        val dx = (x - other.x).toDouble()
        val dy = (y - other.y).toDouble()
        val dz = (z - other.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * 获取玩家所在的方块坐标
     * @return 玩家脚下的 BlockPos
     */
    @JvmStatic
    fun ServerPlayer.locationBlockPos(): BlockPos {
        return blockPosition()
    }

    /**
     * 获取指定位置球形范围内的所有方块坐标
     * @param level 世界
     * @param center 中心坐标
     * @param radius 半径（方块数）
     * @return 球形范围内的所有 BlockPos
     */
    @JvmStatic
    fun getBlocksInRadius(level: ServerLevel, center: BlockPos, radius: Int): List<BlockPos> {
        val result = mutableListOf<BlockPos>()
        val radiusSq = radius * radius
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    if (x * x + y * y + z * z <= radiusSq) {
                        result.add(center.offset(x, y, z))
                    }
                }
            }
        }
        return result
    }

    /**
     * 获取指定位置球形范围内的所有实体
     * @param level 世界
     * @param center 中心坐标
     * @param radius 半径
     * @return 范围内的实体列表
     */
    @JvmStatic
    fun getEntitiesInRadius(level: ServerLevel, center: Vec3, radius: Double): List<Entity> {
        val aabb = AABB(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius, center.z + radius
        )
        return level.getEntitiesOfClass(Entity::class.java, aabb) { entity ->
            entity.position().distanceTo(center) <= radius
        }
    }
}
