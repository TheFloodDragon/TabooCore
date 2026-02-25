package taboocore.proxy

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Entity.RemovalReason
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.GoalSelector
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.core.Holder
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.UUID
import taboocore.reflect.getPrivate

/**
 * 实体代理，封装 NMS Entity 的常用操作
 */
open class EntityProxy(val handle: Entity) {

    /** 实体唯一标识 */
    val uuid: UUID
        get() = handle.uuid

    /** 实体类型 */
    val type: EntityType<*>
        get() = handle.type

    /** 实体位置（Vec3） */
    var pos: Vec3
        get() = handle.position()
        set(value) {
            handle.setPos(value.x, value.y, value.z)
        }

    /** 实体所在方块坐标 */
    var blockPos: BlockPos
        get() = handle.blockPosition()
        set(value) {
            handle.setPos(value.x.toDouble() + 0.5, value.y.toDouble(), value.z.toDouble() + 0.5)
        }

    /** 实体所在的世界 */
    var level: ServerLevel
        get() = handle.level() as ServerLevel
        set(_) {
            // 切换维度需要通过 teleport 完成
        }

    /** 实体是否存活 */
    val isAlive: Boolean
        get() = handle.isAlive

    /** 实体是否已死亡/被移除 */
    val isDead: Boolean
        get() = !handle.isAlive

    /** 是否隐身 */
    var isInvisible: Boolean
        get() = handle.isInvisible
        set(value) {
            handle.setInvisible(value)
        }

    /** 是否发光 */
    var isGlowing: Boolean
        get() = handle.isCurrentlyGlowing
        set(value) {
            handle.setGlowingTag(value)
        }

    /** 是否受重力影响 */
    var hasGravity: Boolean
        get() = !handle.isNoGravity
        set(value) {
            handle.setNoGravity(!value)
        }

    /** 是否着火 */
    var isOnFire: Boolean
        get() = handle.isOnFire
        set(value) {
            if (value) {
                handle.setRemainingFireTicks(200) // 10 秒
            } else {
                handle.setRemainingFireTicks(0)
            }
        }

    /** 自定义名称 */
    var customName: Component?
        get() = handle.customName
        set(value) {
            handle.setCustomName(value)
        }

    /** 是否显示自定义名称 */
    var isCustomNameVisible: Boolean
        get() = handle.isCustomNameVisible
        set(value) {
            handle.setCustomNameVisible(value)
        }

    /**
     * 传送实体到指定维度和坐标
     * @param level 目标维度
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     */
    fun teleport(level: ServerLevel, x: Double, y: Double, z: Double) {
        if (handle is ServerPlayer) {
            (handle as ServerPlayer).teleportTo(level, x, y, z, setOf(), handle.yRot, handle.xRot, true)
        } else {
            handle.teleportTo(x, y, z)
        }
    }

    /**
     * 传送实体到指定位置
     * @param pos 目标位置
     * @param level 目标维度，默认当前维度
     */
    fun teleport(pos: Vec3, level: ServerLevel = this.level) {
        teleport(level, pos.x, pos.y, pos.z)
    }

    /**
     * 移除实体
     */
    fun remove() {
        handle.remove(RemovalReason.DISCARDED)
    }

    /**
     * 添加标签
     * @param tag 标签名
     */
    fun addTag(tag: String) {
        handle.addTag(tag)
    }

    /**
     * 移除标签
     * @param tag 标签名
     */
    fun removeTag(tag: String) {
        handle.removeTag(tag)
    }

    /**
     * 是否拥有指定标签
     * @param tag 标签名
     * @return 是否拥有
     */
    fun hasTag(tag: String): Boolean {
        return try {
            @Suppress("UNCHECKED_CAST")
            handle.getPrivate<Set<String>>("tags").contains(tag)
        } catch (_: Exception) {
            false
        }
    }

    /** 实体所有标签 */
    val tags: Set<String>
        get() = try {
            @Suppress("UNCHECKED_CAST")
            handle.getPrivate<Set<String>>("tags")
        } catch (_: Exception) {
            emptySet()
        }

    /**
     * 向跟踪此实体的玩家发送数据包
     * @param packet 要发送的数据包
     */
    @Suppress("UNCHECKED_CAST")
    fun sendPacket(packet: Packet<*>) {
        val serverLevel = handle.level() as? ServerLevel ?: return
        serverLevel.chunkSource.sendToTrackingPlayers(
            handle,
            packet as Packet<in ClientGamePacketListener>
        )
    }

    /**
     * 计算到另一个实体的距离
     * @param other 另一个实体
     * @return 距离（方块）
     */
    fun distanceTo(other: Entity): Double {
        return handle.distanceTo(other).toDouble()
    }

    /**
     * 是否在指定范围内
     * @param other 另一个实体
     * @param range 范围
     * @return 是否在范围内
     */
    fun isInRange(other: Entity, range: Double): Boolean {
        return handle.distanceToSqr(other) <= range * range
    }

    /**
     * 获取范围内的所有实体
     * @param range 范围（方块）
     * @return 实体列表
     */
    fun nearbyEntities(range: Double): List<Entity> {
        val pos = handle.position()
        val box = AABB(
            pos.x - range, pos.y - range, pos.z - range,
            pos.x + range, pos.y + range, pos.z + range
        )
        return handle.level().getEntities(handle, box) { true }
    }

    /**
     * 获取范围内的所有玩家
     * @param range 范围（方块）
     * @return 玩家列表
     */
    fun nearbyPlayers(range: Double): List<ServerPlayer> {
        val serverLevel = handle.level() as? ServerLevel ?: return emptyList()
        return serverLevel.getPlayers { player ->
            handle.distanceToSqr(player) <= range * range
        }
    }

    // ======================== 乘骑 ========================

    /** 当前乘客列表 */
    val passengers: List<Entity>
        get() = handle.passengers

    /** 当前载具 */
    val vehicle: Entity?
        get() = handle.vehicle

    /**
     * 让指定实体骑乘此实体
     * @param entity 要骑乘的实体
     */
    fun addPassenger(entity: Entity) {
        entity.startRiding(handle, true, true)
    }

    /**
     * 弹出所有乘客
     */
    fun ejectPassengers() {
        handle.ejectPassengers()
    }

    // ======================== 碰撞箱 ========================

    /** 获取实体当前碰撞箱（绝对坐标 AABB） */
    val boundingBox: AABB
        get() = handle.boundingBox

    /** 获取实体类型定义的基础碰撞箱尺寸（未受姿态等状态影响） */
    val baseDimensions: EntityDimensions
        get() = handle.type.dimensions

    /** 获取碰撞箱宽度 */
    val bbWidth: Float
        get() = handle.bbWidth

    /** 获取碰撞箱高度 */
    val bbHeight: Float
        get() = handle.bbHeight

    /**
     * 手动设置实体碰撞箱（慎用，会覆盖原始碰撞箱）
     * @param box 新的碰撞箱
     */
    fun setBoundingBox(box: AABB) {
        handle.setBoundingBox(box)
    }

    /**
     * 判断两个实体碰撞箱是否相交
     * @param other 另一个实体
     * @return 碰撞箱是否相交
     */
    fun intersects(other: EntityProxy): Boolean {
        return handle.boundingBox.intersects(other.handle.boundingBox)
    }

    /**
     * 判断实体碰撞箱是否与指定 AABB 相交
     * @param box 目标 AABB
     * @return 是否相交
     */
    fun intersects(box: AABB): Boolean {
        return handle.boundingBox.intersects(box)
    }

    /**
     * 判断点是否在碰撞箱内
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 点是否在碰撞箱内
     */
    fun containsPoint(x: Double, y: Double, z: Double): Boolean {
        return handle.boundingBox.contains(x, y, z)
    }

    /**
     * 以实体为中心，将碰撞箱向外膨胀指定半径构造新 AABB
     * @param range 膨胀半径
     * @return 膨胀后的 AABB
     */
    fun expandedBoundingBox(range: Double): AABB {
        return handle.boundingBox.inflate(range)
    }

    /**
     * 判断实体是否与某个方块的碰撞形状相交
     * @param pos 方块坐标
     * @return 是否碰撞
     */
    fun collidesWithBlock(pos: BlockPos): Boolean {
        val level = handle.level()
        val blockShape = level.getBlockState(pos).getCollisionShape(level, pos)
        if (blockShape.isEmpty) return false
        return blockShape.toAabbs().any { aabb ->
            aabb.move(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()).intersects(handle.boundingBox)
        }
    }
}

/**
 * 生物代理（扩展 EntityProxy），封装 Mob 的 AI 和目标相关操作
 */
class MobProxy(private val mob: Mob) : EntityProxy(mob) {

    /** 当前攻击目标 */
    var target: LivingEntity?
        get() = mob.target
        set(value) {
            mob.target = value
        }

    /** 是否处于攻击状态 */
    val isAggressive: Boolean
        get() = mob.isAggressive

    /**
     * 清除所有 AI 目标
     */
    fun clearGoals() {
        val goalField = Mob::class.java.getDeclaredField("goalSelector")
        goalField.isAccessible = true
        val goalSelector = goalField.get(mob) as GoalSelector
        goalSelector.removeAllGoals { true }

        val targetField = Mob::class.java.getDeclaredField("targetSelector")
        targetField.isAccessible = true
        val targetSelector = targetField.get(mob) as GoalSelector
        targetSelector.removeAllGoals { true }
    }
}

/**
 * 生命实体代理（扩展 EntityProxy），封装 LivingEntity 的生命值、效果和装备操作
 */
class LivingEntityProxy(private val living: LivingEntity) : EntityProxy(living) {

    /** 当前生命值 */
    var health: Float
        get() = living.health
        set(value) {
            living.health = value
        }

    /** 最大生命值（只读，需通过属性修改） */
    val maxHealth: Float
        get() = living.maxHealth

    /** 伤害吸收量 */
    var absorptionAmount: Float
        get() = living.absorptionAmount
        set(value) {
            living.absorptionAmount = value
        }

    /** 箭矢数量（插在身上的箭） */
    var arrowCount: Int
        get() = living.arrowCount
        set(value) {
            living.arrowCount = value
        }

    /** 是否正在睡觉 */
    val isSleeping: Boolean
        get() = living.isSleeping

    // ======================== 药水效果 ========================

    /** 当前所有活跃的药水效果 */
    val activeEffects: Collection<MobEffectInstance>
        get() = living.activeEffects

    /**
     * 添加药水效果
     * @param effect 效果实例
     * @return 是否成功添加
     */
    fun addEffect(effect: MobEffectInstance): Boolean {
        return living.addEffect(effect, null)
    }

    /**
     * 移除指定类型的药水效果
     * @param effectType 效果类型
     * @return 是否成功移除
     */
    fun removeEffect(effectType: Holder<MobEffect>): Boolean {
        return living.removeEffect(effectType)
    }

    /**
     * 清除所有药水效果
     */
    fun clearEffects() {
        living.removeAllEffects()
    }

    /**
     * 是否拥有指定药水效果
     * @param effectType 效果类型
     * @return 是否拥有
     */
    fun hasEffect(effectType: Holder<MobEffect>): Boolean {
        return living.hasEffect(effectType)
    }

    /**
     * 治疗实体
     * @param amount 治疗量
     */
    fun heal(amount: Float) {
        living.heal(amount)
    }

    /**
     * 对实体造成伤害
     * @param source 伤害来源
     * @param amount 伤害量
     */
    fun damage(source: DamageSource, amount: Float) {
        living.hurt(source, amount)
    }

    // ======================== 装备 ========================

    /** 主手物品 */
    val mainHandItem: ItemStack
        get() = living.mainHandItem

    /** 副手物品 */
    val offHandItem: ItemStack
        get() = living.offhandItem

    /**
     * 获取指定装备槽的物品
     * @param slot 装备槽
     * @return 该槽位的物品
     */
    fun getItemInSlot(slot: EquipmentSlot): ItemStack {
        return living.getItemBySlot(slot)
    }

    /**
     * 设置指定装备槽的物品
     * @param slot 装备槽
     * @param item 物品
     */
    fun setItemInSlot(slot: EquipmentSlot, item: ItemStack) {
        living.setItemSlot(slot, item)
    }
}

// ======================== 扩展函数 ========================

/**
 * 将 Entity 包装为 EntityProxy
 */
fun Entity.proxy(): EntityProxy = EntityProxy(this)

/**
 * 将 LivingEntity 包装为 LivingEntityProxy
 */
fun LivingEntity.proxy(): LivingEntityProxy = LivingEntityProxy(this)

/**
 * 将 Mob 包装为 MobProxy
 */
fun Mob.proxy(): MobProxy = MobProxy(this)
