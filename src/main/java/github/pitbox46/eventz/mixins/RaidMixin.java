package github.pitbox46.eventz.mixins;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.data.contestant.EventContestant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.UUID;

@Mixin(Raid.class)
public class RaidMixin {
    @Shadow private BlockPos center;

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/player/ServerPlayerEntity.addStat(Lnet/minecraft/util/ResourceLocation;)V"), method = "tick", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onPlayerWin(CallbackInfo ci, boolean flag, int i, boolean flag3, int k, Iterator<?> var5, UUID uuid, Entity entity, LivingEntity livingentity, ServerPlayerEntity serverplayerentity) {
        EventContestant contestant;
        if(Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(serverplayerentity)) != null && contestant.hasUnfilledCondition("raid")) {
            //JSObject previousValues, JSObject globalData, String contestantName, int centerX, int centerY, int centerZ
            Eventz.activeEvent.trigger(contestant, "raid", center.getX(), center.getY(), center.getZ());
        }
    }
}
