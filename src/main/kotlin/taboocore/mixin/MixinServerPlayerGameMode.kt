package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerPlayerGameMode
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.level.GameType
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import taboocore.event.player.PlayerGameModeChangeEvent
import taboocore.event.world.BlockBreakEvent
import taboocore.event.world.BlockDamageEvent
import taboocore.event.world.BlockPlaceEvent

@Mixin(ServerPlayerGameMode::class)
abstract class MixinServerPlayerGameMode {

    @Shadow
    lateinit var level: ServerLevel

    @Shadow
    lateinit var player: ServerPlayer

    @Unique
    private var cachedBlockState: BlockState? = null

    @Unique
    private var placeTargetPos: BlockPos? = null

    @Unique
    private var placeOldState: BlockState? = null

    // ============ BlockBreakEvent ============

    @Inject(method = ["destroyBlock"], at = [At("HEAD")], cancellable = true)
    private fun onDestroyBlock(pos: BlockPos, cir: CallbackInfoReturnable<Boolean>) {
        val state = level.getBlockState(pos)
        cachedBlockState = state
        if (BlockBreakEvent.fireBlockBreakPre(player, pos, state)) {
            cachedBlockState = null
            cir.returnValue = false
        }
    }

    @Inject(method = ["destroyBlock"], at = [At("RETURN")])
    private fun onDestroyBlockPost(pos: BlockPos, cir: CallbackInfoReturnable<Boolean>) {
        val state = cachedBlockState ?: return
        cachedBlockState = null
        if (cir.returnValue == true) {
            BlockBreakEvent.fireBlockBreakPost(player, pos, state)
        }
    }

    // ============ BlockPlaceEvent ============

    @Inject(method = ["useItemOn"], at = [At("HEAD")], cancellable = true)
    private fun onUseItemOnPre(
        player: ServerPlayer,
        level: Level,
        itemStack: ItemStack,
        hand: InteractionHand,
        hitResult: BlockHitResult,
        cir: CallbackInfoReturnable<InteractionResult>
    ) {
        val targetPos = hitResult.blockPos.relative(hitResult.direction)
        placeTargetPos = targetPos
        placeOldState = level.getBlockState(targetPos)
    }

    @Inject(method = ["useItemOn"], at = [At("RETURN")])
    private fun onUseItemOnPost(
        player: ServerPlayer,
        level: Level,
        itemStack: ItemStack,
        hand: InteractionHand,
        hitResult: BlockHitResult,
        cir: CallbackInfoReturnable<InteractionResult>
    ) {
        val pos = placeTargetPos ?: return
        val oldState = placeOldState ?: return
        placeTargetPos = null
        placeOldState = null

        val newState = level.getBlockState(pos)
        if (newState != oldState && !newState.isAir) {
            if (BlockPlaceEvent.fireBlockPlacePre(this.player, pos, newState)) {
                level.setBlock(pos, oldState, 3)
                cir.returnValue = InteractionResult.FAIL
            } else {
                BlockPlaceEvent.fireBlockPlacePost(this.player, pos, newState)
            }
        }
    }

    // ============ PlayerGameModeChangeEvent ============

    @Shadow
    private lateinit var gameModeForPlayer: GameType

    @Inject(method = ["changeGameModeForPlayer"], at = [At("HEAD")], cancellable = true)
    private fun onChangeGameModePre(newMode: GameType, cir: CallbackInfoReturnable<Boolean>) {
        if (newMode == gameModeForPlayer) return
        if (PlayerGameModeChangeEvent.firePre(player, gameModeForPlayer, newMode)) {
            cir.returnValue = false
        }
    }

    @Unique
    private var cachedOldGameMode: GameType? = null

    @Inject(
        method = ["changeGameModeForPlayer"],
        at = [At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V")]
    )
    private fun onChangeGameModeCapture(newMode: GameType, cir: CallbackInfoReturnable<Boolean>) {
        cachedOldGameMode = gameModeForPlayer
    }

    @Inject(method = ["changeGameModeForPlayer"], at = [At("RETURN")])
    private fun onChangeGameModePost(newMode: GameType, cir: CallbackInfoReturnable<Boolean>) {
        val oldMode = cachedOldGameMode ?: return
        cachedOldGameMode = null
        if (cir.returnValue == true) {
            PlayerGameModeChangeEvent.firePost(player, oldMode, newMode)
        }
    }

    // ============ BlockDamageEvent ============

    @Unique
    private var blockDamageInstaBreak: Boolean? = null

    /**
     * 拦截 handleBlockBreakAction 方法，玩家开始破坏方块时触发 BlockDamageEvent
     * handleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence)
     */
    @Inject(method = ["handleBlockBreakAction"], at = [At("HEAD")], cancellable = true)
    private fun onHandleBlockBreakActionPre(
        pos: BlockPos,
        action: ServerboundPlayerActionPacket.Action,
        direction: Direction,
        maxY: Int,
        sequence: Int,
        ci: CallbackInfo
    ) {
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return
        val block = level.getBlockState(pos)
        if (block.isAir) return
        val item = player.mainHandItem.copy()
        val instaBreak = player.abilities.instabuild
        val event = BlockDamageEvent.firePre(player, level, pos, block, direction, item, instaBreak)
        if (event == null) {
            ci.cancel()
        } else {
            blockDamageInstaBreak = event.instaBreak
        }
    }

    @Inject(method = ["handleBlockBreakAction"], at = [At("RETURN")])
    private fun onHandleBlockBreakActionPost(
        pos: BlockPos,
        action: ServerboundPlayerActionPacket.Action,
        direction: Direction,
        maxY: Int,
        sequence: Int,
        ci: CallbackInfo
    ) {
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return
        val instaBreak = blockDamageInstaBreak ?: return
        blockDamageInstaBreak = null
        val block = level.getBlockState(pos)
        val item = player.mainHandItem.copy()
        BlockDamageEvent.firePost(player, level, pos, block, direction, item, instaBreak)
    }
}
