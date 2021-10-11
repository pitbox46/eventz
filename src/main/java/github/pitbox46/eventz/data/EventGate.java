package github.pitbox46.eventz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.EventzScriptException;
import github.pitbox46.eventz.EventzScriptLoadingException;
import github.pitbox46.eventz.network.PacketHandler;
import github.pitbox46.eventz.network.server.SSendBoundaryInfo;
import github.pitbox46.eventz.network.server.SStopBoundary;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;

public class EventGate extends HashMap<String, Condition> {
    public final String description;
    public final Operator operator;
    public final boolean global;
    public final int index;
    /**
     * For use with global setting. Tells new contestants to move past the gate.
     */
    public boolean globalCompleted = false;
    public boolean enabled = false;

    public EventGate(String description, Operator operator, boolean global, int index) {
        this.description = description;
        this.operator = operator;
        this.global = global;
        this.index = index;
    }

    public static EventGate readEventGate(JsonObject jsonObject, int index) throws EventzScriptLoadingException {
        EventGate eventGate;
        try {
            String description = jsonObject.get("description").getAsString();
            Operator operator = Operator.valueOf(jsonObject.get("operator").getAsString());
            boolean global = jsonObject.get("global").getAsBoolean();
            JsonArray array = jsonObject.get("conditions").getAsJsonArray();
            eventGate = new EventGate(description, operator, global, index);
            for (JsonElement element : array) {
                Condition condition = Condition.readCondition((JsonObject) element, eventGate);
                eventGate.put(condition.trigger, condition);
            }
        } catch (NullPointerException | UnsupportedOperationException e) {
            throw new EventzScriptLoadingException(e.getMessage(), e);
        }
        return eventGate;
    }

    public EventGate clone() {
        EventGate clone = new EventGate(description, operator, global, index);
        for (Entry<String, Condition> entry : this.entrySet()) {
            clone.put(entry.getKey(), entry.getValue().clone(clone));
        }
        return clone;
    }

    public void enable(List<ServerPlayerEntity> playerList) {
        if (!enabled) {
            try {
                for (Condition condition : this.values()) {
                    condition.startScript();
                }
            } catch (EventzScriptException e) {
                Eventz.activeEvent.stopError();
                e.printStackTrace();
                return;
            }
            enabled = true;
        }
        this.values().forEach(c -> {
            if (!Double.isNaN(c.boundaryMinX)) {
                playerList.forEach(player -> PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SSendBoundaryInfo(c.boundaryMinX, c.boundaryMaxX, c.boundaryMinZ, c.boundaryMaxZ)));
            }
        });
    }

    public void onComplete(List<ServerPlayerEntity> playerList) {
        if (global) {
            Eventz.getServer().getPlayerList().getPlayers().forEach(player -> this.forEach((string, condition) -> {
                if (!Double.isNaN(condition.boundaryMinX))
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

    public enum Operator {
        OR,
        AND
    }
}
