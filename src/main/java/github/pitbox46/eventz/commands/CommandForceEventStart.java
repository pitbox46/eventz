package github.pitbox46.eventz.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.EventRegistration;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandForceEventStart {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Command<CommandSource> CMD_RANDOM = context -> {
        if(Eventz.activeEvent == null)
            ServerEvents.startRandomEvent();
        return 0;
    };

    private static final Command<CommandSource> CMD_KEY = context -> {
        if(Eventz.activeEvent == null) {
            String key = StringArgumentType.getString(context, "eventKey");
            if(EventRegistration.EVENTS.containsKey(key)) {
                ServerEvents.startSpecificEvent(key);
            } else {
                throw new SimpleCommandExceptionType(new LiteralMessage("Could not find key: " + key)).create();
            }
        }
        return 0;
    };

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("forceStart")
                .requires(s -> s.hasPermissionLevel(2))
                .then(Commands.argument("eventKey", StringArgumentType.word())
                        .executes(CMD_KEY))//TODO THIS
                .executes(CMD_RANDOM);
    }
}
