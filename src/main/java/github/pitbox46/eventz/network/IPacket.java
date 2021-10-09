package github.pitbox46.eventz.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface IPacket {
    IPacket readPacketData(PacketBuffer buf);

    void writePacketData(PacketBuffer buf);

    void processPacket(NetworkEvent.Context ctx);
}
