package github.pitbox46.eventz.data.contestant;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.data.Condition;
import github.pitbox46.eventz.data.EventGate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class EventContestant {
    public int gateNumber = 0;
    public HashMap<String, MutablePair<Condition, Boolean>> conditions = new HashMap<String, MutablePair<Condition, Boolean>>() {
        @Override
        public MutablePair<Condition, Boolean> get(Object key) {
            return Objects.requireNonNull(super.get(key), "Could not find key: " + key);
        }
    };

    public EventContestant() {
        Eventz.activeEvent.event.gates.get(0).forEach((trigger, condition) -> conditions.put(trigger, new MutablePair<>(condition, false)));
    }

    abstract public void onStartGate(EventGate gate);

    abstract public String getName();

    abstract public void sendMessage(ITextComponent message);

    static protected ITextComponent formatMessage(String msg) {
        return new StringTextComponent(msg).mergeStyle(TextFormatting.GRAY);
    }

    public boolean hasUnfilledCondition(String condition) {
        return conditions.containsKey(condition) && !conditions.get(condition).getRight();
    }

    abstract public List<ServerPlayerEntity> getPlayers();

    public void iterate() {
        gateNumber++;
        conditions.clear();
        if (Eventz.activeEvent.event.gates.size() > gateNumber) {
            Eventz.activeEvent.event.gates.get(gateNumber).forEach((trigger, condition) -> conditions.put(trigger, new MutablePair<>(condition, false)));
        }
    }
}
