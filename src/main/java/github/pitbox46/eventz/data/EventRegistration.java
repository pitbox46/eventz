package github.pitbox46.eventz.data;

import com.google.gson.*;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.EventzScriptException;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraftforge.fml.loading.FileUtils;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EventRegistration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    //Always create a copy of the event to prevent data from spilling over when the same event is called twice.
    public static final Map<String, Event> EVENTS = new HashMap<String, Event>() {
        @Override
        public Event get(Object key) {
            return super.get(key).clone();
        }

        @Override
        public Event getOrDefault(Object key, Event defaultValue) {
            return containsKey(key) ? get(key) : defaultValue;
        }
    };
    public static final Map<String, CompiledScript> SCRIPTS = new HashMap<>();
    public static String randomizer_function;

    public static void register(File folder) {
        File scripts = FileUtils.getOrCreateDirectory(new File(folder, "scripts").toPath(), "scripts").toFile();
        File eventsJson = new File(folder, "events.json");
        try {
            if (eventsJson.createNewFile()) {
                FileWriter writer = new FileWriter(eventsJson);
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("randomizer_function", new JsonPrimitive("DEFAULT"));
                writer.write(GSON.toJson(jsonObject));
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SCRIPTS.clear();
        SCRIPTS.putAll(Objects.requireNonNull(registerScripts(scripts)));
        SCRIPTS.values().forEach(s -> {
            try {
                s.eval();
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        });
        EVENTS.clear();
        EVENTS.putAll(Objects.requireNonNull(registerEvents(eventsJson)));
    }

    public static Map<String, Event> registerEvents(File jsonFile) {
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
            Map<String, Event> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if(entry.getKey().equals("randomizer_function")) {
                    randomizer_function = entry.getValue().getAsString();
                } else {
                    map.put(entry.getKey(), Event.readEvent(entry.getKey(), entry.getValue().getAsJsonObject()));
                }
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, CompiledScript> registerScripts(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                Map<String, CompiledScript> map = new HashMap<>();
                for (File file : files) {
                    if (file.getName().endsWith(".js")) {
                        try (FileReader reader = new FileReader(file)) {
                            map.put(file.getName(), Eventz.NASHORN.compile(reader));
                        } catch (ScriptException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return map;
            }
        }
        return null;
    }

    public static String getRandomizerEvent(Object... params) throws EventzScriptException {
        if (!randomizer_function.isEmpty()) {
            String[] triggerFunctionPair = randomizer_function.split("#");
            if (triggerFunctionPair.length != 2)
                throw new RuntimeException(String.format("Randomizer function is not in the form \"scriptName.js#functionName\""));
            CompiledScript script = EventRegistration.SCRIPTS.get(triggerFunctionPair[0]);
            if (script == null) {
                throw new EventzScriptException("Could not find script file: " + triggerFunctionPair[0]);
            }
            try {
                Object returnValue = ((Invocable) script.getEngine()).invokeFunction(triggerFunctionPair[1], params);
                if (returnValue instanceof String)
                    return (String) returnValue;
            } catch (ScriptException | NoSuchMethodException e) {
                throw new EventzScriptException(e.getMessage(), e);
            }
        }
        return null;
    }
}
