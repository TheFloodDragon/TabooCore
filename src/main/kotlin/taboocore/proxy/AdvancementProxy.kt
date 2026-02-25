package taboocore.proxy

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * 成就工具，提供成就的授予、撤销和查询功能
 */
object AdvancementUtils {

    /**
     * 检查玩家是否已完成指定成就
     * @param id 成就 ID（例如 "minecraft:story/mine_stone"）
     * @return 是否已完成
     */
    fun ServerPlayer.hasAdvancement(id: String): Boolean {
        val holder = findAdvancement(this, id) ?: return false
        return getAdvancements().getOrStartProgress(holder).isDone
    }

    /**
     * 授予玩家指定成就（授予所有条件）
     * @param id 成就 ID（例如 "minecraft:story/mine_stone"）
     * @return 是否有至少一个条件被授予
     */
    fun ServerPlayer.grantAdvancement(id: String): Boolean {
        val holder = findAdvancement(this, id) ?: return false
        val progress = getAdvancements().getOrStartProgress(holder)
        var granted = false
        for (criterion in progress.remainingCriteria) {
            if (getAdvancements().award(holder, criterion)) {
                granted = true
            }
        }
        return granted
    }

    /**
     * 撤销玩家指定成就（撤销所有条件）
     * @param id 成就 ID（例如 "minecraft:story/mine_stone"）
     * @return 是否有至少一个条件被撤销
     */
    fun ServerPlayer.revokeAdvancement(id: String): Boolean {
        val holder = findAdvancement(this, id) ?: return false
        val progress = getAdvancements().getOrStartProgress(holder)
        var revoked = false
        // 需要复制条件列表，因为撤销会修改进度
        val criteria = holder.value().criteria().keys.toList()
        for (criterion in criteria) {
            if (getAdvancements().revoke(holder, criterion)) {
                revoked = true
            }
        }
        return revoked
    }

    /**
     * 获取玩家指定成就的进度
     * @param id 成就 ID
     * @return 成就进度，成就不存在返回 null
     */
    fun ServerPlayer.getAdvancementProgress(id: String): AdvancementProgress? {
        val holder = findAdvancement(this, id) ?: return null
        return getAdvancements().getOrStartProgress(holder)
    }

    /**
     * 获取当前世界所有已注册的成就
     * @return 成就列表
     */
    fun ServerLevel.getAllAdvancements(): List<AdvancementHolder> {
        return server.advancements.allAdvancements.toList()
    }

    /**
     * 根据 ID 查找成就
     */
    private fun findAdvancement(player: ServerPlayer, id: String): AdvancementHolder? {
        return player.level().server.advancements.get(Identifier.parse(id))
    }
}
