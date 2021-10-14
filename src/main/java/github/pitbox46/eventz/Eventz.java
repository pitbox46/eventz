package github.pitbox46.eventz;

import github.pitbox46.eventz.data.ActiveEvent;
import github.pitbox46.eventz.network.ClientProxy;
import github.pitbox46.eventz.network.CommonProxy;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("eventz")
public class Eventz {
    public static final Random RANDOM = new Random();
    public static final Logger LOGGER = LogManager.getLogger();
    public static final NashornScriptEngine NASHORN = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
    private static final CompiledScript DEFAULT_OBJECT_SCRIPT;
    public static ActiveEvent activeEvent;
    public static CommonProxy PROXY;
    private static MinecraftServer server;

    static {
        try {
            DEFAULT_OBJECT_SCRIPT = Eventz.NASHORN.compile(
                    "function getDefaultConditionObject() {return {\"metaData\":{},\"startData\":{}};}"
                            + "function getDefaultEventObject() {return {\"startData\":{}};}"
                            + "function createEmptyObject() {return {};}");
        } catch (ScriptException e) {
            throw new RuntimeException();
        }
    }

    public Eventz() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Eventz.ClientEvents::init);
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

    public static Object getOrCreateProperty(JSObject object, String property) {
        if (!object.hasMember(property)) {
            object.setMember(property, createEmptyObject());
        }
        return object.getMember(property);
    }

    public static JSObject createEmptyObject() {
        return getDefaultObject("createEmptyObject");
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        Eventz.server = server;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        public static void init(FMLClientSetupEvent event) {
            RenderTypeLookup.setRenderLayer(Registration.INTERFACE_BLOCK.get(), RenderType.getTranslucent());
        }
    }
}
