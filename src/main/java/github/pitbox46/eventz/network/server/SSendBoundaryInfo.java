package github.pitbox46.eventz.network.server;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SSendBoundaryInfo implements IPacket {
    public double minX;
    public double maxX;
    public double minZ;
    public double maxZ;

    public SSendBoundaryInfo() {}

    public SSendBoundaryInfo(double minX, double maxX, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    @Override
    public SSendBoundaryInfo readPacketData(PacketBuffer buf) {
        minX = buf.readDouble();
        maxX = buf.readDouble();
        minZ = buf.readDouble();
        maxZ = buf.readDouble();
        return this;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeDouble(minX).writeDouble(maxX).writeDouble(minZ).writeDouble(maxZ);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Eventz.PROXY.handleSSendBoundaryInfo(ctx, this);
    }
}
