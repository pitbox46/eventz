package github.pitbox46.eventz.data.contestant;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.EventGate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ServerContestant extends EventContestant {
    @Override
    public void onStartGate(EventGate gate) {
        ServerEvents.sendGlobalMsg(formatMessage(gate.description));
    }

    @Override
    public List<ServerPlayerEntity> getPlayers() {
        return Eventz.getServer().getPlayerList().getPlayers();
    }

    @Override
    public String getName() {
        return "SERVER";
    }
}
