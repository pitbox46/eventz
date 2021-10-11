package github.pitbox46.eventz.mixins;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.data.contestant.EventContestant;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(at = @At(value = "HEAD"), method = "onBlockActivated")
    public void onOnBlockActivated(World worldIn, PlayerEntity player, Hand handIn, BlockRayTraceResult resultIn, CallbackInfoReturnable<ActionResultType> cir) {
        if (worldIn.isRemote()) return;
        EventContestant contestant;
        if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant((ServerPlayerEntity) player)) != null && contestant.hasUnfilledCondition("block_activated")) {
            //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String blockName, int posX, int posY, int posZ
            Eventz.activeEvent.trigger(contestant, "block_activated", player.getUniqueID().toString(), player.getGameProfile().getName(), ((AbstractBlock.AbstractBlockState) (Object) this).getBlock().getRegistryName().toString(), resultIn.getPos().getX(), resultIn.getPos().getY(), resultIn.getPos().getZ());
        }
    }
}
