package net.starly.discordauth.context.message;

import net.starly.discordauth.context.message.enums.MessageType;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * Code From: 동욱 & 대영
 * Migrated By: 예준
 */
public class MessageContext {

    private static MessageContext instance;

    public static MessageContext getInstance() {
        if (instance == null) instance = new MessageContext();
        return instance;
    }

    private final Map<MessageType, Map<String, String>> map = new HashMap<>();

    private MessageContext() {
    }

    public STMessage get(MessageType type, String key, String def) {
        return new STMessage(getPrefix(), map.getOrDefault(type, new HashMap<>()).getOrDefault(key, def).replace("{prefix}", getPrefix()));
    }

    public STMessage get(MessageType type, String key) {
        return get(type, key, "");
    }

    public String getOnlyString(MessageType type, String key) {
        return map.getOrDefault(type, new HashMap<>()).getOrDefault(key, "");
    }

    public STMessage get(MessageType type, String key, String def, Function<String, String> replacer) {
        return new STMessage(getPrefix(), replacer.apply(get(type, key, def).getMessage()).replace("{prefix}", getPrefix()));
    }

    public STMessage get(MessageType type, String key, Function<String, String> replacer) {
        return get(type, key, "", replacer);
    }

    public void set(MessageType type, String key, String value) {
        Map<String, String> typeMap = map.getOrDefault(type, new HashMap<>());
        typeMap.put(key, ChatColor.translateAlternateColorCodes('&', value));
        map.put(type, typeMap);
    }

    public void reset() {
        map.clear();
    }


    private String getPrefix() {
        return map.getOrDefault(MessageType.NORMAL, new HashMap<>()).getOrDefault("prefix", "");
    }
}
