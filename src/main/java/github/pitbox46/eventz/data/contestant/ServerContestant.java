package github.pitbox46.eventz.data.contestant;

import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.EventGate;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ServerContestant extends EventContestant {
    @Override
    public void onStartGate(EventGate gate) {
        ServerEvents.sendGlobalMsg(formatMessage(gate.description));
    }

    @Override
    public String getName() {
        return "SERVER";
    }
}
