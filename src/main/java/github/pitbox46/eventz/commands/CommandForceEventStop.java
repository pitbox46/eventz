package github.pitbox46.eventz.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.eventz.Eventz;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandForceEventStop implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandForceEventStop CMD = new CommandForceEventStop();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("forceStop")
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(Eventz.activeEvent != null)
            Eventz.activeEvent.stop();
        return 0;
    }
}
