package taboocore.mixin

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.food.FoodData
import net.minecraft.world.food.FoodProperties
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.player.FoodLevelChangeEvent

@Mixin(FoodData::class)
abstract class MixinFoodData {

    @Shadow
    var foodLevel: Int = 20

    // ========== eat(int, float) ==========

    @Unique
    private var eatReentry: Boolean = false

    @Inject(method = ["eat(IF)V"], at = [At("HEAD")], cancellable = true)
    private fun onEatPre(food: Int, saturationModifier: Float, ci: CallbackInfo) {
        if (eatReentry) return
        // FoodData 本身没有持有 ServerPlayer 引用，我们需要通过 tick 方法的 player 参数捕获
        val player = cachedPlayer ?: return
        val newFoodLevel = (foodLevel + food).coerceIn(0, 20)
        val event = FoodLevelChangeEvent.firePre(player, newFoodLevel, null)
        if (event == null) {
            ci.cancel()
            return
        }
        // 如果事件处理器修改了 foodLevel，应用修改后的值
        if (event.foodLevel != newFoodLevel) {
            ci.cancel()
            val adjustedFood = event.foodLevel - foodLevel
            eatReentry = true
            try {
                (this as FoodData).eat(adjustedFood, saturationModifier)
            } finally {
                eatReentry = false
            }
        }
    }

    @Inject(method = ["eat(IF)V"], at = [At("RETURN")])
    private fun onEatPost(food: Int, saturationModifier: Float, ci: CallbackInfo) {
        val player = cachedPlayer ?: return
        FoodLevelChangeEvent.firePost(player, foodLevel, null)
    }

    // ========== setFoodLevel ==========

    @Unique
    private var setFoodLevelReentry: Boolean = false

    @Inject(method = ["setFoodLevel"], at = [At("HEAD")], cancellable = true)
    private fun onSetFoodLevelPre(food: Int, ci: CallbackInfo) {
        if (setFoodLevelReentry) return
        val player = cachedPlayer ?: return
        if (food == foodLevel) return
        val event = FoodLevelChangeEvent.firePre(player, food, null)
        if (event == null) {
            ci.cancel()
            return
        }
        // 如果事件处理器修改了 foodLevel，应用修改后的值
        if (event.foodLevel != food) {
            ci.cancel()
            setFoodLevelReentry = true
            try {
                (this as FoodData).setFoodLevel(event.foodLevel)
            } finally {
                setFoodLevelReentry = false
            }
        }
    }

    @Inject(method = ["setFoodLevel"], at = [At("RETURN")])
    private fun onSetFoodLevelPost(food: Int, ci: CallbackInfo) {
        val player = cachedPlayer ?: return
        FoodLevelChangeEvent.firePost(player, food, null)
    }

    // ========== tick — 缓存 player 引用 ==========

    @Unique
    private var cachedPlayer: ServerPlayer? = null

    @Inject(method = ["tick"], at = [At("HEAD")])
    private fun onTickPre(player: ServerPlayer, ci: CallbackInfo) {
        cachedPlayer = player
    }

    @Inject(method = ["tick"], at = [At("RETURN")])
    private fun onTickPost(player: ServerPlayer, ci: CallbackInfo) {
        cachedPlayer = null
    }
}
