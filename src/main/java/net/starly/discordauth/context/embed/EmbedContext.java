package net.starly.discordauth.context.embed;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashMap;
import java.util.Map;

public class EmbedContext {

    private static EmbedContext instance;
    public static EmbedContext getInstance() {
        if (instance == null) instance = new EmbedContext();
        return instance;
    }
    private EmbedContext() {}

    private final Map<String, MessageEmbed> map = new HashMap<>();

    public MessageEmbed get(String key) {
        return map.get(key);
    }

    public void set(String key, MessageEmbed value) {
        map.put(key, value);
    }

    public void reset() {
        map.clear();
    }
}
