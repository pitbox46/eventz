package github.pitbox46.eventz.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("eventz")
                        .then(CommandForceEventStart.register(dispatcher))
                        .then(CommandForceEventStop.register(dispatcher))
                        .then(CommandReloadScripts.register(dispatcher))
        );

        dispatcher.register(Commands.literal("eventz").redirect(cmdTut));
    }
}
