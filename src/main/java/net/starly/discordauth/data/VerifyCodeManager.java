package net.starly.discordauth.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VerifyCodeManager {

    private static VerifyCodeManager instance;
    public static VerifyCodeManager getInstance() {
        if (instance == null) instance = new VerifyCodeManager();
        return instance;
    }

    private final Map<String, UUID> map = new HashMap<>();

    public UUID get(String verifyCode) {
        return map.get(verifyCode);
    }

    public void set(String verifyCode, UUID playerId) {
        map.put(verifyCode, playerId);
    }

    public boolean has(String verifyCode) {
        return map.containsKey(verifyCode);
    }

    public boolean has(UUID playerId) {
        return map.containsValue(playerId);
    }

    public void remove(String verifyCode) {
        map.remove(verifyCode);
    }

    public void reset() {
        map.clear();
    }
}
