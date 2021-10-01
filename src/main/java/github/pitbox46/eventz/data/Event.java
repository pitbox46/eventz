package github.pitbox46.eventz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.ArrayList;

public class Event {
    private static final Logger LOGGER = LogManager.getLogger();

    public final String name;
    public final String title;
    public final String description;
    public final String startMethod;
    public final int winners;
    public final long duration;
    public final long monetaReward;
    public final ItemStack itemReward;
    public final Type type;
    public final ArrayList<EventGate> gates;

    public Event(String name, String title, String description, String startMethod, int winners, long duration, long monetaReward, ItemStack itemReward, Type type, ArrayList<EventGate> gates) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.startMethod = startMethod;
        this.winners = winners;
        this.duration = duration;
        this.monetaReward = monetaReward;
        this.itemReward = itemReward;
        this.type = type;
        this.gates = gates;
    }

    public void startScript() {
        if(!startMethod.isEmpty()) {
            String[] scriptFunctionPair = startMethod.split("#");
            if(scriptFunctionPair.length != 2)
                throw new RuntimeException(String.format("Start method for event %s is not in the form \"scriptName.js#functionName\"", name));
            CompiledScript script = EventRegistration.SCRIPTS.get(scriptFunctionPair[0]);
            try {
                Object returnValue = ((Invocable) script.getEngine()).invokeFunction(scriptFunctionPair[1]);
                //TODO this
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static Event readEvent(String name, JsonObject jsonObject) {
        try {
            String title = jsonObject.get("title").getAsString();
            String description = jsonObject.get("description").getAsString();
            String startMethod = jsonObject.get("start_method").getAsString();
            int winners = jsonObject.get("winners").getAsInt();
            long duration = jsonObject.get("duration").getAsLong();
            long monetaReward = jsonObject.get("moneta_reward").getAsLong();
            String itemRewardString = jsonObject.get("item_reward").getAsString();
            Type type = Type.valueOf(jsonObject.get("type").getAsString());
            ItemStack itemReward = ItemStack.EMPTY;
            if(itemRewardString != null && !itemRewardString.isEmpty())
                itemReward = ItemStack.read(new JsonToNBT(new StringReader(jsonObject.get("item_reward").getAsString())).readStruct());
            JsonArray jsonArray = jsonObject.get("gates").getAsJsonArray();
            ArrayList<EventGate> gates = new ArrayList<>();
            for(JsonElement jsonElement : jsonArray) {
                gates.add(EventGate.readEventGate(jsonElement.getAsJsonObject()));
            }
            return new Event(name, title, description, startMethod, winners, duration, monetaReward, itemReward, type, gates);
        } catch (UnsupportedOperationException | CommandSyntaxException e) {
            LOGGER.warn("There was an issue processing the event!");
            e.printStackTrace();
        }
        return null;
    }

    public enum Type {
        TEAM,
        INDIVIDUAL,
        SERVER
    }
}
