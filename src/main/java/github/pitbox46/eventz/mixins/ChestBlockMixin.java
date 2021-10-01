package github.pitbox46.eventz.mixins;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.data.contestant.EventContestant;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.openContainer(Lnet/minecraft/inventory/container/INamedContainerProvider;)Ljava/util/OptionalInt;"), method = "onBlockActivated")
    public void onOnBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, CallbackInfoReturnable<ActionResultType> cir) {
        EventContestant contestant;
        if(Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant((ServerPlayerEntity) player)) != null && contestant.hasUnfilledCondition("locked_loot") && contestant.conditions.get("locked_loot").getLeft().genericPos.equals(pos)) {
            //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, int posX, int posY, int posZ
            Eventz.activeEvent.trigger(contestant, "locked_loot", (player).getUniqueID().toString(), (player).getGameProfile().getName(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
