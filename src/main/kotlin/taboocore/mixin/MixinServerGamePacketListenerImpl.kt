package taboocore.mixin

import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket
import net.minecraft.network.protocol.game.ServerboundChatPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.network.protocol.game.ServerboundSwingPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.inventory.InventoryClickEvent
import taboocore.event.inventory.InventoryCloseEvent
import taboocore.event.player.*
import taboocore.event.world.SignChangeEvent

@Mixin(ServerGamePacketListenerImpl::class)
abstract class MixinServerGamePacketListenerImpl {

    @Shadow
    lateinit var player: ServerPlayer

    // ========== Chat ==========

    @Unique
    private var chatMessage: String? = null

    @Inject(method = ["handleChat"], at = [At("HEAD")], cancellable = true)
    private fun onHandleChat(packet: ServerboundChatPacket, ci: CallbackInfo) {
        chatMessage = packet.message()
        if (PlayerChatEvent.firePre(player, packet.message())) {
            chatMessage = null
            ci.cancel()
        }
    }

    @Inject(method = ["handleChat"], at = [At("RETURN")])
    private fun onHandleChatPost(packet: ServerboundChatPacket, ci: CallbackInfo) {
        val msg = chatMessage ?: return
        chatMessage = null
        PlayerChatEvent.firePost(player, msg)
    }

    // ========== Command (unsigned) ==========

    @Unique
    private var commandString: String? = null

    @Inject(method = ["handleChatCommand"], at = [At("HEAD")], cancellable = true)
    private fun onHandleChatCommand(packet: ServerboundChatCommandPacket, ci: CallbackInfo) {
        commandString = packet.command()
        if (PlayerCommandEvent.firePre(player, packet.command())) {
            commandString = null
            ci.cancel()
        }
    }

    @Inject(method = ["handleChatCommand"], at = [At("RETURN")])
    private fun onHandleChatCommandPost(packet: ServerboundChatCommandPacket, ci: CallbackInfo) {
        val cmd = commandString ?: return
        commandString = null
        PlayerCommandEvent.firePost(player, cmd)
    }

    // ========== Command (signed) ==========

    @Inject(method = ["handleSignedChatCommand"], at = [At("HEAD")], cancellable = true)
    private fun onHandleSignedChatCommand(packet: ServerboundChatCommandSignedPacket, ci: CallbackInfo) {
        commandString = packet.command()
        if (PlayerCommandEvent.firePre(player, packet.command())) {
            commandString = null
            ci.cancel()
        }
    }

    @Inject(method = ["handleSignedChatCommand"], at = [At("RETURN")])
    private fun onHandleSignedChatCommandPost(packet: ServerboundChatCommandSignedPacket, ci: CallbackInfo) {
        val cmd = commandString ?: return
        commandString = null
        PlayerCommandEvent.firePost(player, cmd)
    }

    // ========== Move ==========

    @Unique
    private var moveFromX: Double = 0.0
    @Unique
    private var moveFromY: Double = 0.0
    @Unique
    private var moveFromZ: Double = 0.0
    @Unique
    private var moveFired: Boolean = false

    @Inject(method = ["handleMovePlayer"], at = [At("HEAD")], cancellable = true)
    private fun onHandleMovePlayer(packet: ServerboundMovePlayerPacket, ci: CallbackInfo) {
        moveFired = false
        val fromX = player.x
        val fromY = player.y
        val fromZ = player.z
        val toX = packet.getX(fromX)
        val toY = packet.getY(fromY)
        val toZ = packet.getZ(fromZ)
        // Only fire if the player actually moved
        if (fromX == toX && fromY == toY && fromZ == toZ) return
        moveFromX = fromX
        moveFromY = fromY
        moveFromZ = fromZ
        moveFired = true
        if (PlayerMoveEvent.firePre(player, fromX, fromY, fromZ, toX, toY, toZ)) {
            moveFired = false
            ci.cancel()
        }
    }

    @Inject(method = ["handleMovePlayer"], at = [At("RETURN")])
    private fun onHandleMovePlayerPost(packet: ServerboundMovePlayerPacket, ci: CallbackInfo) {
        if (!moveFired) return
        moveFired = false
        PlayerMoveEvent.firePost(
            player, moveFromX, moveFromY, moveFromZ,
            player.x, player.y, player.z
        )
    }

    // ========== Interact (use item on block / RIGHT_CLICK_BLOCK) ==========

    @Unique
    private var interactFired: Boolean = false
    @Unique
    private var interactAction: PlayerInteractEvent.Action? = null
    @Unique
    private var interactHand: net.minecraft.world.InteractionHand? = null
    @Unique
    private var interactItem: net.minecraft.world.item.ItemStack? = null
    @Unique
    private var interactBlock: net.minecraft.world.level.block.state.BlockState? = null
    @Unique
    private var interactBlockPos: net.minecraft.core.BlockPos? = null
    @Unique
    private var interactBlockFace: net.minecraft.core.Direction? = null

    @Inject(method = ["handleUseItemOn"], at = [At("HEAD")], cancellable = true)
    private fun onHandleUseItemOn(packet: ServerboundUseItemOnPacket, ci: CallbackInfo) {
        val hand = packet.getHand()
        val hitResult = packet.getHitResult()
        val pos = hitResult.blockPos
        val face = hitResult.direction
        val item = player.getItemInHand(hand).copy()
        val block = player.level().getBlockState(pos)
        interactFired = true
        interactAction = PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
        interactHand = hand
        interactItem = item
        interactBlock = block
        interactBlockPos = pos
        interactBlockFace = face
        if (PlayerInteractEvent.firePre(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, item, hand, block, pos, face)) {
            clearInteractState()
            ci.cancel()
        }
    }

    @Inject(method = ["handleUseItemOn"], at = [At("RETURN")])
    private fun onHandleUseItemOnPost(packet: ServerboundUseItemOnPacket, ci: CallbackInfo) {
        if (!interactFired) return
        PlayerInteractEvent.firePost(player, interactAction!!, interactItem!!, interactHand, interactBlock, interactBlockPos, interactBlockFace)
        clearInteractState()
    }

    // ========== Interact (use item in air / RIGHT_CLICK_AIR) ==========

    @Inject(method = ["handleUseItem"], at = [At("HEAD")], cancellable = true)
    private fun onHandleUseItem(packet: ServerboundUseItemPacket, ci: CallbackInfo) {
        val hand = packet.getHand()
        val item = player.getItemInHand(hand).copy()
        interactFired = true
        interactAction = PlayerInteractEvent.Action.RIGHT_CLICK_AIR
        interactHand = hand
        interactItem = item
        interactBlock = null
        interactBlockPos = null
        interactBlockFace = null
        if (PlayerInteractEvent.firePre(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, item, hand, null, null, null)) {
            clearInteractState()
            ci.cancel()
        }
    }

    @Inject(method = ["handleUseItem"], at = [At("RETURN")])
    private fun onHandleUseItemPost(packet: ServerboundUseItemPacket, ci: CallbackInfo) {
        if (!interactFired) return
        PlayerInteractEvent.firePost(player, interactAction!!, interactItem!!, interactHand, null, null, null)
        clearInteractState()
    }

    // ========== Interact (LEFT_CLICK_AIR via arm swing) ==========

    @Inject(method = ["handleAnimate"], at = [At("HEAD")], cancellable = true)
    private fun onHandleAnimate(packet: ServerboundSwingPacket, ci: CallbackInfo) {
        val hand = packet.getHand()
        val item = player.getItemInHand(hand).copy()
        if (PlayerInteractEvent.firePre(player, PlayerInteractEvent.Action.LEFT_CLICK_AIR, item, hand, null, null, null)) {
            ci.cancel()
        }
    }

    @Inject(method = ["handleAnimate"], at = [At("RETURN")])
    private fun onHandleAnimatePost(packet: ServerboundSwingPacket, ci: CallbackInfo) {
        val hand = packet.getHand()
        val item = player.getItemInHand(hand).copy()
        PlayerInteractEvent.firePost(player, PlayerInteractEvent.Action.LEFT_CLICK_AIR, item, hand, null, null, null)
    }

    @Unique
    private fun clearInteractState() {
        interactFired = false
        interactAction = null
        interactHand = null
        interactItem = null
        interactBlock = null
        interactBlockPos = null
        interactBlockFace = null
    }

    // ========== Sneak & Sprint (via player input) ==========

    @Unique
    private var previousShift: Boolean = false
    @Unique
    private var previousSprint: Boolean = false

    @Inject(method = ["handlePlayerInput"], at = [At("HEAD")], cancellable = true)
    private fun onHandlePlayerInput(packet: ServerboundPlayerInputPacket, ci: CallbackInfo) {
        val newShift = packet.input().shift()
        val newSprint = packet.input().sprint()
        previousShift = player.isCrouching
        previousSprint = player.isSprinting
        // Sneak toggle
        if (newShift != previousShift) {
            if (PlayerToggleSneakEvent.firePre(player, newShift)) {
                ci.cancel()
                return
            }
        }
        // Sprint toggle
        if (newSprint != previousSprint) {
            if (PlayerToggleSprintEvent.firePre(player, newSprint)) {
                ci.cancel()
                return
            }
        }
    }

    @Inject(method = ["handlePlayerInput"], at = [At("RETURN")])
    private fun onHandlePlayerInputPost(packet: ServerboundPlayerInputPacket, ci: CallbackInfo) {
        val newShift = packet.input().shift()
        val newSprint = packet.input().sprint()
        if (newShift != previousShift) {
            PlayerToggleSneakEvent.firePost(player, newShift)
        }
        if (newSprint != previousSprint) {
            PlayerToggleSprintEvent.firePost(player, newSprint)
        }
    }

    // ========== Sprint (via handlePlayerCommand START_SPRINTING / STOP_SPRINTING) ==========

    @Inject(method = ["handlePlayerCommand"], at = [At("HEAD")], cancellable = true)
    private fun onHandlePlayerCommand(packet: ServerboundPlayerCommandPacket, ci: CallbackInfo) {
        when (packet.getAction()) {
            ServerboundPlayerCommandPacket.Action.START_SPRINTING -> {
                if (PlayerToggleSprintEvent.firePre(player, true)) {
                    ci.cancel()
                }
            }
            ServerboundPlayerCommandPacket.Action.STOP_SPRINTING -> {
                if (PlayerToggleSprintEvent.firePre(player, false)) {
                    ci.cancel()
                }
            }
            else -> {}
        }
    }

    @Inject(method = ["handlePlayerCommand"], at = [At("RETURN")])
    private fun onHandlePlayerCommandPost(packet: ServerboundPlayerCommandPacket, ci: CallbackInfo) {
        when (packet.getAction()) {
            ServerboundPlayerCommandPacket.Action.START_SPRINTING -> {
                PlayerToggleSprintEvent.firePost(player, true)
            }
            ServerboundPlayerCommandPacket.Action.STOP_SPRINTING -> {
                PlayerToggleSprintEvent.firePost(player, false)
            }
            else -> {}
        }
    }

    // ========== Drop Item & Swap Hand Items (via handlePlayerAction) ==========

    @Unique
    private var dropAll: Boolean? = null
    @Unique
    private var dropItem: net.minecraft.world.item.ItemStack? = null
    @Unique
    private var swapFired: Boolean = false
    @Unique
    private var leftClickBlockFired: Boolean = false

    @Inject(method = ["handlePlayerAction"], at = [At("HEAD")], cancellable = true)
    private fun onHandlePlayerAction(packet: ServerboundPlayerActionPacket, ci: CallbackInfo) {
        dropAll = null
        dropItem = null
        swapFired = false
        leftClickBlockFired = false
        when (packet.getAction()) {
            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> {
                val pos = packet.getPos()
                val face = packet.getDirection()
                val block = player.level().getBlockState(pos)
                val item = player.mainHandItem.copy()
                leftClickBlockFired = true
                if (PlayerInteractEvent.firePre(player, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, item, net.minecraft.world.InteractionHand.MAIN_HAND, block, pos, face)) {
                    leftClickBlockFired = false
                    ci.cancel()
                }
            }
            ServerboundPlayerActionPacket.Action.DROP_ITEM -> {
                val item = player.mainHandItem.copy()
                dropAll = false
                dropItem = item
                if (PlayerDropItemEvent.firePre(player, item, false)) {
                    dropAll = null
                    dropItem = null
                    ci.cancel()
                }
            }
            ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS -> {
                val item = player.mainHandItem.copy()
                dropAll = true
                dropItem = item
                if (PlayerDropItemEvent.firePre(player, item, true)) {
                    dropAll = null
                    dropItem = null
                    ci.cancel()
                }
            }
            ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND -> {
                swapFired = true
                if (PlayerSwapHandItemsEvent.firePre(player)) {
                    swapFired = false
                    ci.cancel()
                }
            }
            else -> {}
        }
    }

    @Inject(method = ["handlePlayerAction"], at = [At("RETURN")])
    private fun onHandlePlayerActionPost(packet: ServerboundPlayerActionPacket, ci: CallbackInfo) {
        if (leftClickBlockFired) {
            leftClickBlockFired = false
            val pos = packet.getPos()
            val face = packet.getDirection()
            val block = player.level().getBlockState(pos)
            val item = player.mainHandItem.copy()
            PlayerInteractEvent.firePost(player, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, item, net.minecraft.world.InteractionHand.MAIN_HAND, block, pos, face)
        }
        dropAll?.let { all ->
            val item = dropItem
            this.dropAll = null
            this.dropItem = null
            if (item != null) {
                PlayerDropItemEvent.firePost(player, item, all)
            }
        }
        if (swapFired) {
            swapFired = false
            PlayerSwapHandItemsEvent.firePost(player)
        }
    }

    // ========== Sign Change ==========

    @Unique
    private var signEvent: taboocore.event.world.SignChangeEvent.Pre? = null

    @Inject(method = ["handleSignUpdate"], at = [At("HEAD")], cancellable = true)
    private fun onHandleSignUpdate(packet: ServerboundSignUpdatePacket, ci: CallbackInfo) {
        val level = player.level()
        val pos = packet.getPos()
        val lines = packet.getLines()
        val isFront = packet.isFrontText()
        val event = SignChangeEvent.firePre(player, level, pos, lines, isFront)
        if (event == null) {
            ci.cancel()
        } else {
            signEvent = event
        }
    }

    @Inject(method = ["handleSignUpdate"], at = [At("RETURN")])
    private fun onHandleSignUpdatePost(packet: ServerboundSignUpdatePacket, ci: CallbackInfo) {
        val event = signEvent ?: return
        signEvent = null
        val level = player.level()
        val pos = packet.getPos()
        val isFront = packet.isFrontText()
        // 如果插件修改了 lines，需要更新告示牌文本
        val originalLines = packet.getLines()
        if (!event.lines.contentEquals(originalLines)) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is net.minecraft.world.level.block.entity.SignBlockEntity) {
                val filteredLines = event.lines.map { net.minecraft.server.network.FilteredText.passThrough(it) }
                blockEntity.updateSignText(player, isFront, filteredLines)
            }
        }
        SignChangeEvent.firePost(player, level, pos, event.lines, isFront)
    }

    // ========== Inventory Click ==========

    @Unique
    private var clickFired: Boolean = false

    @Inject(method = ["handleContainerClick"], at = [At("HEAD")], cancellable = true)
    private fun onHandleContainerClick(packet: ServerboundContainerClickPacket, ci: CallbackInfo) {
        clickFired = false
        val container = player.containerMenu
        val containerId = packet.containerId()
        val slotIndex = packet.slotNum().toInt()
        val buttonNum = packet.buttonNum().toInt()
        val containerInput = packet.containerInput()
        val carriedItem = player.containerMenu.carried.copy()
        val event = InventoryClickEvent.firePre(player, containerId, slotIndex, buttonNum, containerInput, carriedItem, container)
        if (event == null) {
            ci.cancel()
            return
        }
        clickFired = true
    }

    @Inject(method = ["handleContainerClick"], at = [At("RETURN")])
    private fun onHandleContainerClickPost(packet: ServerboundContainerClickPacket, ci: CallbackInfo) {
        if (!clickFired) return
        clickFired = false
        val container = player.containerMenu
        val containerId = packet.containerId()
        val slotIndex = packet.slotNum().toInt()
        val buttonNum = packet.buttonNum().toInt()
        val containerInput = packet.containerInput()
        val carriedItem = player.containerMenu.carried.copy()
        InventoryClickEvent.firePost(player, containerId, slotIndex, buttonNum, containerInput, carriedItem, container)
    }

    // ========== Inventory Close ==========

    @Unique
    private var closeFired: Boolean = false

    @Inject(method = ["handleContainerClose"], at = [At("HEAD")], cancellable = true)
    private fun onHandleContainerClose(packet: ServerboundContainerClosePacket, ci: CallbackInfo) {
        closeFired = false
        val containerId = packet.containerId
        val container = player.containerMenu
        if (InventoryCloseEvent.firePre(player, containerId, container)) {
            ci.cancel()
            return
        }
        closeFired = true
    }

    @Inject(method = ["handleContainerClose"], at = [At("RETURN")])
    private fun onHandleContainerClosePost(packet: ServerboundContainerClosePacket, ci: CallbackInfo) {
        if (!closeFired) return
        closeFired = false
        val containerId = packet.containerId
        val container = player.containerMenu
        InventoryCloseEvent.firePost(player, containerId, container)
    }

    // ========== Item Held (slot change) ==========

    @Unique
    private var itemHeldPreviousSlot: Int = -1

    @Unique
    private var itemHeldFired: Boolean = false

    @Inject(method = ["handleSetCarriedItem"], at = [At("HEAD")], cancellable = true)
    private fun onHandleSetCarriedItem(packet: ServerboundSetCarriedItemPacket, ci: CallbackInfo) {
        itemHeldFired = false
        val previousSlot = player.inventory.getSelectedSlot()
        val newSlot = packet.getSlot()
        if (previousSlot == newSlot) return
        itemHeldPreviousSlot = previousSlot
        val event = PlayerItemHeldEvent.firePre(player, previousSlot, newSlot)
        if (event == null) {
            ci.cancel()
            return
        }
        itemHeldFired = true
        // 如果事件处理器修改了 newSlot，应用修改后的值
        if (event.newSlot != newSlot) {
            ci.cancel()
            itemHeldFired = false
            player.inventory.setSelectedSlot(event.newSlot)
            PlayerItemHeldEvent.firePost(player, previousSlot, event.newSlot)
        }
    }

    @Inject(method = ["handleSetCarriedItem"], at = [At("RETURN")])
    private fun onHandleSetCarriedItemPost(packet: ServerboundSetCarriedItemPacket, ci: CallbackInfo) {
        if (!itemHeldFired) return
        itemHeldFired = false
        PlayerItemHeldEvent.firePost(player, itemHeldPreviousSlot, packet.getSlot())
    }

    // ========== Toggle Flight ==========

    @Unique
    private var toggleFlightFired: Boolean = false

    @Unique
    private var previousFlying: Boolean = false

    @Inject(method = ["handlePlayerAbilities"], at = [At("HEAD")], cancellable = true)
    private fun onHandlePlayerAbilities(packet: ServerboundPlayerAbilitiesPacket, ci: CallbackInfo) {
        toggleFlightFired = false
        val isFlying = packet.isFlying() && player.abilities.mayfly
        previousFlying = player.abilities.flying
        if (isFlying == previousFlying) return
        val event = PlayerToggleFlightEvent.firePre(player, isFlying)
        if (event == null) {
            ci.cancel()
            return
        }
        toggleFlightFired = true
    }

    @Inject(method = ["handlePlayerAbilities"], at = [At("RETURN")])
    private fun onHandlePlayerAbilitiesPost(packet: ServerboundPlayerAbilitiesPacket, ci: CallbackInfo) {
        if (!toggleFlightFired) return
        toggleFlightFired = false
        PlayerToggleFlightEvent.firePost(player, player.abilities.flying)
    }

    // ========== Interact Entity ==========

    @Unique
    private var interactEntityFired: Boolean = false

    @Unique
    private var interactEntityTarget: net.minecraft.world.entity.Entity? = null

    @Unique
    private var interactEntityHand: net.minecraft.world.InteractionHand? = null

    @Inject(method = ["handleInteract"], at = [At("HEAD")], cancellable = true)
    private fun onHandleInteract(packet: ServerboundInteractPacket, ci: CallbackInfo) {
        interactEntityFired = false
        val level = player.level()
        val target = level.getEntityOrPart(packet.entityId()) ?: return
        interactEntityTarget = target
        interactEntityHand = packet.hand()
        val event = PlayerInteractEntityEvent.firePre(player, target, packet.hand())
        if (event == null) {
            interactEntityTarget = null
            interactEntityHand = null
            ci.cancel()
            return
        }
        interactEntityFired = true
    }

    @Inject(method = ["handleInteract"], at = [At("RETURN")])
    private fun onHandleInteractPost(packet: ServerboundInteractPacket, ci: CallbackInfo) {
        if (!interactEntityFired) return
        interactEntityFired = false
        val target = interactEntityTarget ?: return
        val hand = interactEntityHand
        interactEntityTarget = null
        interactEntityHand = null
        PlayerInteractEntityEvent.firePost(player, target, hand)
    }
}
