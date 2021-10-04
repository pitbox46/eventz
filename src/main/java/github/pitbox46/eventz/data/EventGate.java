package github.pitbox46.eventz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.EventzScriptException;
import jdk.nashorn.api.scripting.JSObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public void enable() {
        if(enabled) return;
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

    public void onComplete() {
        if(global)
            onGlobalComplete();
    }

    public void onGlobalComplete() {
        globalCompleted = true;
    }

    public static EventGate readEventGate(JsonObject jsonObject) {
        EventGate eventGate = null;
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
            e.printStackTrace();
        }
        return eventGate;
    }

    public enum Operator {
        OR,
        AND
    }
}
