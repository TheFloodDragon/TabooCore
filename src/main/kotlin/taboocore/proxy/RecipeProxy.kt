package taboocore.proxy

import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder

/**
 * 配方工具，提供配方的解锁、锁定和查询功能
 */
object RecipeUtils {

    /**
     * 解锁指定配方
     * @param id 配方 ID（例如 "minecraft:diamond_sword"）
     * @return 是否成功解锁
     */
    fun ServerPlayer.unlockRecipe(id: String): Boolean {
        val recipe = findRecipe(level().server, id) ?: return false
        return awardRecipes(listOf(recipe)) > 0
    }

    /**
     * 锁定指定配方
     * @param id 配方 ID
     */
    fun ServerPlayer.lockRecipe(id: String) {
        val recipe = findRecipe(level().server, id) ?: return
        resetRecipes(listOf(recipe))
    }

    /**
     * 获取玩家已解锁的所有配方
     * @return 已解锁配方列表
     */
    fun ServerPlayer.getAllUnlockedRecipes(): List<RecipeHolder<*>> {
        val recipeManager = level().server.recipeManager
        return recipeManager.recipes.filter { recipe ->
            recipeBook.contains(recipe.id())
        }.toList()
    }

    /**
     * 获取服务器所有配方
     * @return 配方列表
     */
    fun MinecraftServer.getAllRecipes(): List<RecipeHolder<*>> {
        return recipeManager.recipes.toList()
    }

    /**
     * 根据 ID 获取指定配方
     * @param id 配方 ID
     * @return 配方，不存在返回 null
     */
    fun MinecraftServer.getRecipe(id: String): RecipeHolder<*>? {
        return findRecipe(this, id)
    }

    /**
     * 根据 ID 查找配方
     */
    private fun findRecipe(server: MinecraftServer, id: String): RecipeHolder<*>? {
        val key = ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, Identifier.parse(id))
        return server.recipeManager.byKey(key).orElse(null)
    }
}
