package github.pitbox46.eventz.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.monetamoney.commands.CommandAddShop;
import github.pitbox46.monetamoney.commands.CommandLeaveTeam;
import github.pitbox46.monetamoney.commands.CommandListShop;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("eventz")
                        .then(CommandForceEventStart.register(dispatcher))
                        .then(CommandForceEventStop.register(dispatcher))
        );

        dispatcher.register(Commands.literal("eventz").redirect(cmdTut));
    }
}
