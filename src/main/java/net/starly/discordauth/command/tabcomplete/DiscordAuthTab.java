package net.starly.discordauth.command.tabcomplete;

import net.starly.discordauth.DiscordAuthMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordAuthTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("starly.discordauth.gencode")) completions.add("발급");
            if (sender.hasPermission("starly.discordauth.unauth.self") || sender.hasPermission("starly.discordauth.unauth.other")) completions.add("해제");
            if (sender.hasPermission("starly.discordauth.reload")) completions.add("리로드");
        } else if (args.length == 2) {
            if (args[0].equals("해제") && sender.hasPermission("starly.discordauth.unauth.other")) {
                completions.add("<플레이어>");
                completions.addAll(DiscordAuthMain.getInstance().getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            }
        }

        return completions;
    }
}
