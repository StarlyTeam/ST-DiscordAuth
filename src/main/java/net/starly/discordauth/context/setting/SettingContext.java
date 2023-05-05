package net.starly.discordauth.context.setting;

import net.starly.discordauth.context.setting.enums.SettingType;

import java.util.HashMap;
import java.util.Map;

public class SettingContext {

    private static SettingContext instance;
    public static SettingContext getInstance() {
        if (instance == null) instance = new SettingContext();
        return instance;
    }
    private SettingContext() {}

    private final Map<SettingType, Map<String, Object>> map = new HashMap<>();

    public String get(SettingType type, String key) {
        return get(type, key, String.class);
    }

    public <T> T get(SettingType type, String key, Class<T> inst) {
        return inst.cast(map.getOrDefault(type, new HashMap<>()).get(key));
    }

    public void set(SettingType type, String key, Object value) {
        Map<String, Object> typeMap = map.getOrDefault(type, new HashMap<>());
        typeMap.put(key, value);
        map.put(type, typeMap);
    }

    public void reset() {
        map.clear();
    }
}
