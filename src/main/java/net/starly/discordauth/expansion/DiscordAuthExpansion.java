package net.starly.discordauth.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DiscordAuthExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "discordauth";
    }

    @Override
    public @NotNull String getAuthor() {
        return DiscordAuthMain.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return DiscordAuthMain.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("authenticated")) {
            return new PlayerAuthData((Player) player).isAuthenticated() ?
                    ChatColor.translateAlternateColorCodes('&', DiscordAuthMain.config.getString("expansion.true")) :
                    ChatColor.translateAlternateColorCodes('&', DiscordAuthMain.config.getString("expansion.false"));
        }

        return null;
    }
}
