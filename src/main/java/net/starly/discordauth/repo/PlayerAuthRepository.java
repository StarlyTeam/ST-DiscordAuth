package net.starly.discordauth.repo;

import net.starly.discordauth.DiscordAuthMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerAuthRepository {

    private File dataFile;
    private final Map<UUID, String> map = new HashMap();


    @Deprecated
    public void initialize(File dataFile) {
        this.dataFile = dataFile;
        this.map.clear();

        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (IOException ignored) {
            }
        } else {
            FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

            dataConfig.getKeys(false).forEach(key -> {
                UUID playerId;
                try {
                    playerId = UUID.fromString(key);
                } catch (IllegalArgumentException ignored) {
                    DiscordAuthMain.getInstance().getLogger().severe("인증 데이터를 불러올 수 없습니다 : " + key);
                    return;
                }

                String discordId = dataConfig.getString(key);
                if (discordId == null) {
                    dataConfig.set(key, null);
                    return;
                }

                map.put(playerId, discordId);
            });
        }
    }


    public String getDiscordId(UUID playerId) {
        return map.get(playerId);
    }

    public void setDiscordId(UUID playerId, String discordId) {
        if (discordId == null) map.remove(playerId);
        else map.put(playerId, discordId);
    }

    public boolean isAuthenticated(UUID playerId) {
        return map.containsKey(playerId);
    }


    public void saveAll() {
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        map.forEach((playerId, discordId) -> dataConfig.set(String.valueOf(playerId), discordId));

        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
