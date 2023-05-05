package net.starly.discordauth.listener;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.util.TeleportUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!DiscordAuthMain.getPlayerAuthRepository().isAuthenticated(player.getUniqueId())) {
            TeleportUtil.teleport(TeleportUtil.LobbyType.VERIFY_LOBBY, player);
        }
    }
}
