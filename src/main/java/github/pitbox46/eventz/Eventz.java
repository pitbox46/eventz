package github.pitbox46.eventz;

import github.pitbox46.eventz.data.ActiveEvent;
import github.pitbox46.eventz.network.ClientProxy;
import github.pitbox46.eventz.network.CommonProxy;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
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
    public static final Random RANDOM = new Random();
    public static final Logger LOGGER = LogManager.getLogger();
    public static final NashornScriptEngine NASHORN = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
    private static final CompiledScript DEFAULT_OBJECT_SCRIPT;
    private static MinecraftServer server;
    public static ActiveEvent activeEvent;
    public static CommonProxy PROXY;

    static {
        try {
            DEFAULT_OBJECT_SCRIPT = Eventz.NASHORN.compile(
                    "function getDefaultConditionObject() {return {\"meta_data\":{},\"start_data\":{}};}"
                    + "function getDefaultEventObject() {return {\"start_data\":{},\"global_data\":{}};}"
                    + "function createEmptyObject() {return {};}");
        } catch (ScriptException e) {
            throw new RuntimeException();
        }
    }

    public Eventz() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        Registration.init();
    }

    public static JSObject getDefaultObject(String function) {
        try {
            DEFAULT_OBJECT_SCRIPT.eval();
            return (JSObject) ((Invocable) DEFAULT_OBJECT_SCRIPT.getEngine()).invokeFunction(function);
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
            activeEvent.stopError();
        }
        return null;
    }

    public static JSObject createEmptyObject() {
        return getDefaultObject("createEmptyObject");
    }

    public static void setServer(MinecraftServer server) {
        Eventz.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
