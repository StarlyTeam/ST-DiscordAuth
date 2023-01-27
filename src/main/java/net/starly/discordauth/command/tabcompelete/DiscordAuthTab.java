package net.starly.discordauth.command.tabcompelete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordAuthTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return sender.isOp() ? Arrays.asList("리로드", "발급", "해제") : Arrays.asList("발급", "해제");

        return Collections.emptyList();
    }
}
