package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BucketItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LiquidBlockContainer
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.player.PlayerBucketEmptyEvent
import taboocore.event.player.PlayerBucketFillEvent

@Mixin(BucketItem::class)
abstract class MixinBucketItem {

    @Shadow
    @Final
    private lateinit var content: Fluid

    @Unique
    private var bucketFired: Boolean = false

    @Unique
    private var bucketHand: InteractionHand? = null

    @Unique
    private var bucketPos: BlockPos? = null

    // ========== PlayerBucketFillEvent / PlayerBucketEmptyEvent ==========

    @Inject(method = ["use"], at = [At("HEAD")], cancellable = true)
    private fun onUsePre(level: Level, player: Player, hand: InteractionHand, cir: CallbackInfoReturnable<InteractionResult>) {
        if (player !is ServerPlayer) return
        if (level.isClientSide) return

        val itemStack = player.getItemInHand(hand)
        // 模拟 Item.getPlayerPOVHitResult 射线检测
        val from = player.eyePosition
        val viewVec = player.calculateViewVector(player.xRot, player.yRot)
        val to = from.add(viewVec.scale(player.blockInteractionRange()))
        val fluidMode = if (content == Fluids.EMPTY) ClipContext.Fluid.SOURCE_ONLY else ClipContext.Fluid.NONE
        val hitResult = level.clip(ClipContext(from, to, ClipContext.Block.OUTLINE, fluidMode, player))

        if (hitResult.type != HitResult.Type.BLOCK) return

        val pos = hitResult.blockPos
        bucketHand = hand
        bucketFired = false

        if (content == Fluids.EMPTY) {
            // 装取（Fill）
            val block = level.getBlockState(pos)
            bucketPos = pos
            val event = PlayerBucketFillEvent.firePre(player, hand, pos, block, itemStack.copy())
            if (event == null) {
                bucketPos = null
                bucketHand = null
                cir.returnValue = InteractionResult.FAIL
                return
            }
            bucketFired = true
        } else {
            // 倒出（Empty）
            val clickedState = level.getBlockState(pos)
            val placePos = if (clickedState.block is LiquidBlockContainer && content == Fluids.WATER) pos
            else pos.relative(hitResult.direction)
            bucketPos = placePos
            val block = level.getBlockState(placePos)
            val event = PlayerBucketEmptyEvent.firePre(player, hand, placePos, block, itemStack.copy())
            if (event == null) {
                bucketPos = null
                bucketHand = null
                cir.returnValue = InteractionResult.FAIL
                return
            }
            bucketFired = true
        }
    }

    @Inject(method = ["use"], at = [At("RETURN")])
    private fun onUsePost(level: Level, player: Player, hand: InteractionHand, cir: CallbackInfoReturnable<InteractionResult>) {
        if (!bucketFired) return
        bucketFired = false
        if (player !is ServerPlayer) return

        val pos = bucketPos ?: return
        val h = bucketHand ?: return
        bucketPos = null
        bucketHand = null

        val block = level.getBlockState(pos)
        val item = player.getItemInHand(h)
        if (content == Fluids.EMPTY) {
            PlayerBucketFillEvent.firePost(player, h, pos, block, item)
        } else {
            PlayerBucketEmptyEvent.firePost(player, h, pos, block, item)
        }
    }
}
