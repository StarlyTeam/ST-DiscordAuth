package net.starly.discordauth.event;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!DiscordAuthMain.config.getBoolean("other_settings.enable_cancellation_move")) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        Player player = event.getPlayer();
        PlayerAuthData data = new PlayerAuthData(player);
        if (!data.isAuthenticated()) event.setCancelled(true);
    }
}
