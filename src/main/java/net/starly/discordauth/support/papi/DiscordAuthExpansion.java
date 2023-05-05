package net.starly.discordauth.support.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class DiscordAuthExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "discordauth";
    }

    @Override
    public @NotNull String getAuthor() {
        return DiscordAuthMain.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return DiscordAuthMain.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("auth")) {
            return ChatColor.translateAlternateColorCodes('&',
                    DiscordAuthMain.getPlayerAuthRepository().isAuthenticated(player.getUniqueId())
                            ? SettingContext.getInstance().get(SettingType.CONFIG, "text.papi.auth.true")
                            : SettingContext.getInstance().get(SettingType.CONFIG, "text.papi.auth.false")
            );
        }

        return null;
    }
}
