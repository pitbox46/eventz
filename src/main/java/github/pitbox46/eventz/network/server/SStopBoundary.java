package github.pitbox46.eventz.network.server;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SStopBoundary implements IPacket {
    public SStopBoundary() {}

    @Override
    public SStopBoundary readPacketData(PacketBuffer buf) {
        return this;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {}

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Eventz.PROXY.handleSStopBoundary(ctx, this);
    }
}
