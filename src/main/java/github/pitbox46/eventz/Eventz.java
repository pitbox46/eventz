package github.pitbox46.eventz;

import github.pitbox46.eventz.data.ActiveEvent;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Random;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("eventz")
public class Eventz {
    private static MinecraftServer server;
    public static final Random RANDOM = new Random();
    public static final Logger LOGGER = LogManager.getLogger();
    public static final NashornScriptEngine NASHORN = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
    private static final CompiledScript DEFAULT_OBJECT_SCRIPT;
    public static final Supplier<JSObject> DEFAULT_OBJECT_SUPPLIER;
    public static ActiveEvent activeEvent;

    static {
        try {
            DEFAULT_OBJECT_SCRIPT = Eventz.NASHORN.compile("function getDefaultObject() {return {\"meta_data\":{},\"start_data\":{},\"global_data\":{}};}");
        } catch (ScriptException e) {
            throw new RuntimeException();
        }
        DEFAULT_OBJECT_SUPPLIER = () -> {
            try {
                DEFAULT_OBJECT_SCRIPT.eval();
                return (JSObject) ((Invocable) DEFAULT_OBJECT_SCRIPT.getEngine()).invokeFunction("getDefaultObject");
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public Eventz() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        Registration.init();
    }

    public static void setServer(MinecraftServer server) {
        Eventz.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
