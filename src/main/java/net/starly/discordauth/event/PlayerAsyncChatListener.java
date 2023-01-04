package net.starly.discordauth.event;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerAsyncChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (new PlayerAuthData(player).isAuthenticated()) return;
        if (player.isOp()) return;

        event.setCancelled(true);
        DiscordAuthMain.config.getMessages("other_settings.chat_cancelled_message").forEach(player::sendMessage);
    }
}
