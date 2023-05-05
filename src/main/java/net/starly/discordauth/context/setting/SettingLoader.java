package net.starly.discordauth.context.setting;

import net.starly.core.util.PreCondition;
import net.starly.discordauth.context.setting.enums.SettingType;
import org.bukkit.configuration.file.FileConfiguration;

public class SettingLoader {

    public static void loadSettingFile(FileConfiguration config, SettingType type) {
        PreCondition.nonNull(config, "설정을 로드할 수 없습니다. : " + type.name());

        SettingContext settingContext = SettingContext.getInstance();
        config.getKeys(true).forEach(key -> {
            if (config.isConfigurationSection(key)) return;

            settingContext.set(type, key, config.get(key));
        });
    }
}