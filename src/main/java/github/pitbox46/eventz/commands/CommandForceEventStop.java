package github.pitbox46.eventz.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.pitbox46.eventz.Eventz;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandForceEventStop {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Command<CommandSource> CMD = context -> {
        if (Eventz.activeEvent != null)
            Eventz.activeEvent.stop("Event stopped forcefully via command");
        return 0;
    };

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("forceStop")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(CMD);
    }
}
