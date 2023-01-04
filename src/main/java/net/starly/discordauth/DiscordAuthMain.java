package net.starly.discordauth;

import net.starly.core.data.Config;
import net.starly.core.data.MessageConfig;
import net.starly.discordauth.bot.Bot;
import net.starly.discordauth.command.DiscordAuthCmd;
import net.starly.discordauth.data.PlayerAuthData;
import net.starly.discordauth.event.PlayerAsyncChatListener;
import net.starly.discordauth.event.PlayerCommandPreprocessListener;
import net.starly.discordauth.event.PlayerJoinListener;
import net.starly.discordauth.event.PlayerMoveListener;
import net.starly.discordauth.expansion.DiscordAuthExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DiscordAuthMain extends JavaPlugin {
    private static JavaPlugin plugin;
    public static MessageConfig messageConfig;
    public static Config config;
    public static Config botConfig;

    public static Bot bot;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ST-Core") == null) {
            Bukkit.getLogger().warning("[" + this.getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + this.getName() + "] 다운로드 링크 : &fhttps://discord.gg/TF8jqSJjCG");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;


        //Commands
        Bukkit.getPluginCommand("discordauth").setExecutor(new DiscordAuthCmd());
        Bukkit.getPluginCommand("discordauth").setTabCompleter(new DiscordAuthCmd());


        //Configs
        config = new Config("config", plugin);
        config.loadDefaultConfig();
        config.setPrefix("prefix");

        botConfig = new Config("bot", plugin);
        botConfig.loadDefaultConfig();

        Config config = new Config("config", plugin);
        config.loadDefaultConfig();
        messageConfig = new MessageConfig(config, "prefix");


        //EventHandlers
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        if (config.getBoolean("other_settings.enable_cancellation_move")) Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), plugin);


        //Bot
        if (botConfig.getBoolean("bot_settings.enable"))
            bot = new Bot(this, botConfig.getString("bot_settings.token"));


        // Tasks
        if (config.getBoolean("messages.not_verified_title.enable")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerAuthData data = new PlayerAuthData(player);
                        if (data.isAuthenticated()) return;

                        player.sendTitle(ChatColor.translateAlternateColorCodes('&', config.getString("messages.not_verified_title.title")),
                                ChatColor.translateAlternateColorCodes('&', config.getString("messages.not_verified_title.subtitle")),
                                config.getInt("messages.not_verified_title.fade_in") * 20,
                                config.getInt("messages.not_verified_title.stay") * 20,
                                config.getInt("messages.not_verified_title.fade_out") * 20);
                    }
                }
            }.runTaskTimerAsynchronously(this, 0, config.getLong("messages.not_verified_title.interval") * 20L);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getLogger().info("§aPlaceholderAPI 플러그인이 적용되어 있습니다! PlaceholderAPI 확장을 적용합니다.");

            DiscordAuthExpansion discordAuthExpansion = new DiscordAuthExpansion();
            if (discordAuthExpansion.canRegister()) {
                discordAuthExpansion.register();
            }
        } else {
            Bukkit.getLogger().warning("PlaceholderAPI 플러그인이 적용되어 있지 않습니다! PlaceholderAPI 확장을 적용하지 않습니다.");
        }
    }

    @Override
    public void onDisable() {
        if (bot != null)
            bot.getJDA().shutdown();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
