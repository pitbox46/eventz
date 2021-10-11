package github.pitbox46.eventz.mixins;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.data.contestant.EventContestant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(at = @At(value = "HEAD"), method = "onFoodEaten")
    private void onFoodEaten(World world, ItemStack food, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isRemote()) return;
        EventContestant contestant;
        if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant((ServerPlayerEntity) (Object) this)) != null && contestant.hasUnfilledCondition("player_eat_food")) {
            //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String food, int hungerRestore, float saturation
            Eventz.activeEvent.trigger(contestant, "player_eat_food", ((ServerPlayerEntity) (Object) this).getUniqueID().toString(), ((ServerPlayerEntity) (Object) this).getGameProfile().getName(), food.getItem().getRegistryName().toString(), food.getItem().getFood().getHealing(), food.getItem().getFood().getSaturation());
        }
    }
}
