package net.starly.discordauth.listener;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.repo.PlayerAuthRepository;
import net.starly.discordauth.util.TeleportUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerAuthRepository authRepository = DiscordAuthMain.getPlayerAuthRepository();
        if (authRepository.isAuthenticated(player.getUniqueId())) return;

        TeleportUtil.teleport(TeleportUtil.LobbyType.VERIFY_LOBBY, player);
    }
}
