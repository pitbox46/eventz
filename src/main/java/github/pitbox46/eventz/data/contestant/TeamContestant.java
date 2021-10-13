package github.pitbox46.eventz.data.contestant;

import github.pitbox46.eventz.data.EventGate;
import github.pitbox46.monetamoney.data.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class TeamContestant extends EventContestant {
    public final Team team;
    public final List<ServerPlayerEntity> players = new ArrayList<>();

    public TeamContestant(Team team, List<ServerPlayerEntity> players) {
        this.team = team;
        this.players.addAll(players);
    }

    @Override
    public void onStartGate(EventGate gate) {
        sendMessage(formatMessage(gate.description));
    }

    @Override
    public void sendMessage(ITextComponent message) {
        for (ServerPlayerEntity player : players) {
            player.sendStatusMessage(message, false);
        }
    }

    @Override
    public List<ServerPlayerEntity> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public String getName() {
        return team.leader + "'s Team";
    }
}
