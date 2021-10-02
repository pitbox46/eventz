package github.pitbox46.eventz.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.EventRegistration;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.storage.FolderName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandReloadScripts {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Command<CommandSource> CMD = context -> {
        if(Eventz.activeEvent != null) {
            Eventz.activeEvent.stop("Event stopped in order to reload events");
        }
        EventRegistration.register(Eventz.getServer().func_240776_a_(new FolderName("eventz")).toFile());
        return 0;
    };

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("reloadScripts")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(CMD);
    }
}
