package github.pitbox46.eventz.network;

import github.pitbox46.eventz.network.server.SSendBoundaryInfo;
import github.pitbox46.eventz.network.server.SStopBoundary;
import github.pitbox46.monetamoney.network.server.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonProxy {
    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleSSendBoundaryInfo(NetworkEvent.Context ctx, SSendBoundaryInfo packet) {}
    public void handleSStopBoundary(NetworkEvent.Context ctx, SStopBoundary packet) {}

}
