package net.starly.discordauth.util;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.context.message.MessageContext;
import net.starly.discordauth.context.message.STMessage;
import net.starly.discordauth.context.message.enums.MessageType;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportUtil {

    public static void teleport(LobbyType type, Player player) {
        SettingContext settingContext = SettingContext.getInstance();
        MessageContext messageContext = MessageContext.getInstance();

        Location location = null;
        STMessage message = null;
        switch (type) {
            case VERIFY_LOBBY: {
                if (settingContext.get(SettingType.CONFIG, "auth.verifyLobby.enable", Boolean.class)) {
                    location = getLocation("verifyLobby");
                    message = messageContext.get(MessageType.NORMAL, "teleportedVerifyLobby");
                }
                break;
            }

            case LOBBY: {
                if (settingContext.get(SettingType.CONFIG, "auth.lobby.enable", Boolean.class)) {
                    location = getLocation("lobby");
                    message = messageContext.get(MessageType.NORMAL, "teleportedLobby");
                }
                break;
            }

            default: return;
        }

        if (location != null) {
            final int delay = SettingContext.getInstance().get(SettingType.CONFIG, "auth.verifyLobby.delay", Integer.class);
            final Location finalLocation = location;
            final STMessage finalMessage = message;

            new BukkitRunnable() {

                @Override
                public void run() {
                    player.teleport(finalLocation);
                    finalMessage.send(player);
                }
            }.runTaskLater(DiscordAuthMain.getInstance(), delay * 20L);
        }
    }

    private static Location getLocation(String name) {
        SettingContext settingContext = SettingContext.getInstance();

        String worldName = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.world");
        World world = DiscordAuthMain.getInstance().getServer().getWorld(worldName);
        double x = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.x", Double.class);
        double y = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.y", Double.class);
        double z = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.z", Double.class);
        double yaw = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.yaw", Double.class);
        double pitch = settingContext.get(SettingType.CONFIG, "auth." + name + ".location.pitch", Double.class);

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }


    public enum LobbyType {
        VERIFY_LOBBY, LOBBY
    }
}