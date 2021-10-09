package github.pitbox46.eventz.data.contestant;

import github.pitbox46.eventz.data.EventGate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class PlayerContestant extends EventContestant {
    public final ServerPlayerEntity player;

    public PlayerContestant(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void onStartGate(EventGate gate) {
        player.sendStatusMessage(formatMessage(gate.description), false);
    }

    @Override
    public List<ServerPlayerEntity> getPlayers() {
        return Collections.singletonList(player);
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
