package net.starly.discordauth.event;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (DiscordAuthMain.config.getBoolean("location.verify_lobby.enable")) {
            if (new PlayerAuthData(p).isAuthenticated()) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (new PlayerAuthData(p).isAuthenticated()) return;
                    if (!p.isOnline()) return;

                    p.teleport(DiscordAuthMain.config.getLocation("location.verify_lobby.location"));
                    try {
                        DiscordAuthMain.config.getMessages("location.verify_lobby.message").forEach(p::sendMessage);
                    } catch(NoSuchMethodError ex) {
                        Bukkit.getLogger().info("§9[§eDiscordAuth§9] §eST-Core §c플러그인의 버전이 너무 낮습니다. 1.4.1 이상의 버전으로 업데이트 해주세요.");
                        Bukkit.getLogger().info("§9[§eDiscordAuth§9] §eST-Core §f플러그인 다운로드 : §7https://discord.gg/TF8jqSJjCG");
                    }
                }
            }.runTaskLater(DiscordAuthMain.getPlugin(), DiscordAuthMain.config.getInt("location.verify_lobby.delay") * 20L);
        }
    }
}
