package github.pitbox46.eventz.network;

import github.pitbox46.eventz.network.server.SSendBoundaryInfo;
import github.pitbox46.eventz.network.server.SStopBoundary;
import net.minecraft.world.border.BorderStatus;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    private static WorldBorder eventBoundary;

    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static WorldBorder getEventBoundary() {
        return eventBoundary;
    }

    @Override
    public void handleSSendBoundaryInfo(NetworkEvent.Context ctx, SSendBoundaryInfo packet) {
        eventBoundary = new WorldBorder() {
            @Override
            public BorderStatus getStatus() {
                return BorderStatus.STATIONARY;
            }

            @Override
            public double minX() {
                return packet.minX;
            }

            @Override
            public double maxX() {
                return packet.maxX;
            }

            @Override
            public double minZ() {
                return packet.minZ;
            }

            @Override
            public double maxZ() {
                return packet.maxZ;
            }
        };
        eventBoundary.setDamagePerBlock(0);
        eventBoundary.setWarningDistance(0);
        eventBoundary.setWarningTime(0);
        eventBoundary.setDamageBuffer(0);
    }

    @Override
    public void handleSStopBoundary(NetworkEvent.Context ctx, SStopBoundary packet) {
        eventBoundary = null;
    }
}
