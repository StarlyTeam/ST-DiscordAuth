package net.starly.discordauth.data;

import net.starly.core.data.Config;
import net.starly.discordauth.DiscordAuthMain;
import org.bukkit.entity.Player;

public class PlayerAuthData {
    private String discordId;
    private Player player;
    private boolean authenticated;
    private Config data;

    public PlayerAuthData(Player player) {
        this.player = player;
        this.data = new Config("data/" + player.getUniqueId(), DiscordAuthMain.getPlugin());
        data.loadDefaultConfig();

        if (data.getConfig().getKeys(false).size() == 0) {
            data.setString("discordId", "");
            data.setString("playerUuid", player.getUniqueId().toString());
            data.setBoolean("authenticated", false);
            data.saveConfig();
        }

        this.discordId = data.getString("discordId");
        this.authenticated = data.getBoolean("authenticated");
    }

    public String getDiscordId() {
        return discordId;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public Config getData() {
        return data;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setData(Config data) {
        this.data = data;
    }

    public void saveConfig() {
        data.setString("playerUuid", player.getUniqueId().toString());
        data.setString("discordId", discordId);
        data.setBoolean("authenticated", authenticated);
        data.saveConfig();
    }
}
