package github.pitbox46.eventz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.EventzScriptException;
import github.pitbox46.eventz.EventzScriptLoadingException;
import github.pitbox46.eventz.data.contestant.EventContestant;
import github.pitbox46.eventz.network.PacketHandler;
import github.pitbox46.eventz.network.server.SSendBoundaryInfo;
import github.pitbox46.eventz.network.server.SStopBoundary;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static sun.audio.AudioPlayer.player;

public class EventGate extends HashMap<String, Condition> {
    public final String description;
    public final Operator operator;
    public final boolean global;
    /**
     * For use with global setting. Tells new contestants to move past the gate.
     */
    public boolean globalCompleted = false;
    public boolean enabled = false;

    public EventGate(String description, Operator operator, boolean global) {
        this.description = description;
        this.operator = operator;
        this.global = global;
    }

    public EventGate clone() {
        EventGate clone = new EventGate(description, operator, global);
        for(Entry<String, Condition> entry: this.entrySet()) {
            clone.put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
    }

    public void enable(List<ServerPlayerEntity> playerList) {
        if(!enabled) {
            try {
                for (Condition condition : this.values()) {
                    condition.startScript();
                }
            } catch (EventzScriptException e) {
                Eventz.activeEvent.stop("Event stopped due to some issue. Please consult server logs");
                e.printStackTrace();
                return;
            }
            enabled = true;
        }
        this.values().forEach(c -> {
            if(!Double.isNaN(c.boundaryMinX)) {
                playerList.forEach(player -> PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SSendBoundaryInfo(c.boundaryMinX, c.boundaryMaxX, c.boundaryMinZ, c.boundaryMaxZ)));
            }
        });
    }

    public void onComplete(List<ServerPlayerEntity> playerList) {
        if(global) {
            Eventz.getServer().getPlayerList().getPlayers().forEach(player -> this.forEach((string, condition) -> {
                if(!Double.isNaN(condition.boundaryMinX))
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SStopBoundary());
            }));
            onGlobalComplete();
        } else {
            playerList.forEach(player -> this.forEach((string, condition) -> {
                if (!Double.isNaN(condition.boundaryMinX))
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SStopBoundary());
            }));
        }
    }

    public void onGlobalComplete() {
        globalCompleted = true;
    }

    public static EventGate readEventGate(JsonObject jsonObject) throws EventzScriptLoadingException {
        EventGate eventGate;
        try {
            String description = jsonObject.get("description").getAsString();
            Operator operator = Operator.valueOf(jsonObject.get("operator").getAsString());
            boolean global = jsonObject.get("global").getAsBoolean();
            JsonArray array = jsonObject.get("conditions").getAsJsonArray();
            eventGate = new EventGate(description, operator, global);
            for(JsonElement element: array) {
                Condition condition = Condition.readCondition((JsonObject) element);
                assert condition != null;
                eventGate.put(condition.trigger, condition);
            }
        } catch (NullPointerException | UnsupportedOperationException e) {
            throw new EventzScriptLoadingException(e.getMessage(), e);
        }
        return eventGate;
    }

    public enum Operator {
        OR,
        AND
    }
}
