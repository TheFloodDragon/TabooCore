package taboocore.mixin

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import taboocore.event.world.BlockDispenseEvent

@Mixin(DispenserBlock::class)
abstract class MixinDispenserBlock {

    @Unique
    private var dispenseSlot: Int = -1

    @Unique
    private var dispenseItem: ItemStack? = null

    /**
     * 拦截 dispenseFrom 方法，发射器发射前触发 BlockDispenseEvent
     * dispenseFrom(ServerLevel level, BlockState state, BlockPos pos)
     */
    @Inject(method = ["dispenseFrom"], at = [At("HEAD")], cancellable = true)
    private fun onDispenseFromPre(level: ServerLevel, state: BlockState, pos: BlockPos, ci: CallbackInfo) {
        val blockEntity = level.getBlockEntity(pos, BlockEntityType.DISPENSER).orElse(null) ?: return
        val slot = blockEntity.getRandomSlot(level.random)
        if (slot < 0) return
        val item = blockEntity.getItem(slot).copy()
        val direction = state.getValue(DispenserBlock.FACING)
        val event = BlockDispenseEvent.firePre(level, pos, state, item, direction)
        if (event == null) {
            ci.cancel()
        } else {
            dispenseSlot = slot
            dispenseItem = event.item
            // 如果插件修改了 item，替换发射器槽位中的物品
            if (event.item !== item) {
                blockEntity.setItem(slot, event.item)
            }
        }
    }

    /**
     * 拦截 dispenseFrom 方法尾部，发射器发射后触发 Post 事件
     */
    @Inject(method = ["dispenseFrom"], at = [At("TAIL")])
    private fun onDispenseFromPost(level: ServerLevel, state: BlockState, pos: BlockPos, ci: CallbackInfo) {
        val item = dispenseItem ?: return
        dispenseSlot = -1
        dispenseItem = null
        val direction = state.getValue(DispenserBlock.FACING)
        BlockDispenseEvent.firePost(level, pos, state, item, direction)
    }
}
