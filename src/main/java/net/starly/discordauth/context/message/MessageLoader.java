package net.starly.discordauth.context.message;

import net.starly.core.util.PreCondition;
import net.starly.discordauth.context.message.enums.MessageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Objects;

/*
 * Code From: 동욱 & 대영
 * Migrated By: 예준
 */
public class MessageLoader {

    private static boolean loaded = false;

    public static void loadMessageFile(FileConfiguration config) {
        if (loaded) {
            MessageContext.getInstance().reset();
            loaded = false;
        }

        Arrays.stream(MessageType.values()).forEach(type -> loadMessageSection(Objects.requireNonNull(config.getConfigurationSection(type.getPath())), type));

        loaded = true;
    }

    private static void loadMessageSection(ConfigurationSection section, MessageType type) {
        PreCondition.nonNull(section, "메세지를 로드할 수 없습니다. : " + type.name());

        MessageContext msgContext = MessageContext.getInstance();
        section.getKeys(true).forEach(key -> {
            if (section.isConfigurationSection(key)) return;

            if (section.isList(key)) msgContext.set(type, key, String.join("\n&r{prefix}", section.getStringList(key)));
            else msgContext.set(type, key, section.getString(key));
        });
    }
}