package net.starly.discordauth.event;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandPreprocessListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (new PlayerAuthData(player).isAuthenticated()) return;
        if (player.isOp()) return;

        List<String> commands = new ArrayList<>(Bukkit.getPluginCommand("discordauth").getAliases());
        commands.add("discordauth");
        if (commands.contains(event.getMessage()
                .split(" ")[0]
                .replace("/", "")
                .toLowerCase())) return;

        event.setCancelled(true);
        DiscordAuthMain.config.getMessages("other_settings.command_cancelled_message").forEach(player::sendMessage);
    }
}
