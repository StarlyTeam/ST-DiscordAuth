package net.starly.discordauth.runnable;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import net.starly.discordauth.repo.PlayerAuthRepository;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TitleScheduler extends BukkitRunnable {

    private static TitleScheduler instance;

    public static void start(JavaPlugin plugin) {
        instance = new TitleScheduler();
        instance.runTaskTimer(plugin, 0L, SettingContext.getInstance().get(SettingType.CONFIG, "auth.authRequestTitle.interval", Integer.class) * 20L);
    }

    public static void stop() {
        if (instance != null) instance.cancel();
        instance = null;
    }

    @Override
    public void run() {
        SettingContext settingContext = SettingContext.getInstance();
        if (!settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.enable", Boolean.class)) return;

        String title = ChatColor.translateAlternateColorCodes('&', settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.title"));
        String subTitle = ChatColor.translateAlternateColorCodes('&', settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.subTitle"));
        int fadeIn = settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.fadeIn", Integer.class) * 20;
        int stay = settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.stay", Integer.class) * 20;
        int fadeOut = settingContext.get(SettingType.CONFIG, "auth.authRequestTitle.fadeOut", Integer.class) * 20;

        PlayerAuthRepository authRepository = DiscordAuthMain.getPlayerAuthRepository();
        DiscordAuthMain.getInstance().getServer().getOnlinePlayers().forEach(player -> {
            if (authRepository.isAuthenticated(player.getUniqueId())) return;

            player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
        });
    }
}
