package net.starly.discordauth;

import lombok.Getter;
import net.starly.core.bstats.Metrics;
import net.starly.discordauth.bot.DiscordAuthBot;
import net.starly.discordauth.context.embed.EmbedContext;
import net.starly.discordauth.context.embed.EmbedLoader;
import net.starly.discordauth.command.DiscordAuthCmd;
import net.starly.discordauth.command.tabcomplete.DiscordAuthTab;
import net.starly.discordauth.context.message.MessageContext;
import net.starly.discordauth.context.message.MessageLoader;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.data.VerifyCodeManager;
import net.starly.discordauth.listener.PlayerActionBlockListener;
import net.starly.discordauth.listener.PlayerJoinListener;
import net.starly.discordauth.repo.PlayerAuthRepository;
import net.starly.discordauth.context.setting.SettingLoader;
import net.starly.discordauth.context.setting.enums.SettingType;
import net.starly.discordauth.runnable.TitleScheduler;
import net.starly.discordauth.support.papi.DiscordAuthExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DiscordAuthMain extends JavaPlugin {

    @Getter private static DiscordAuthMain instance;
    @Getter private static PlayerAuthRepository playerAuthRepository;
    @Getter private DiscordAuthBot bot;

    @Override
    public void onEnable() {
        /* DEPENDENCY
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (!isPluginEnabled("ST-Core")) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : §fhttp://starly.kr/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        /* SETUP
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        instance = this;
        playerAuthRepository = new PlayerAuthRepository();
        new Metrics(instance, 17295);

        /* CONFIG - GENERAL
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        saveDefaultConfig();
        SettingLoader.loadSettingFile(YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml")), SettingType.CONFIG);

        File messageFile = new File(getDataFolder(), "message.yml");
        if (!messageFile.exists()) saveResource("message.yml", true);
        MessageLoader.loadMessageFile(YamlConfiguration.loadConfiguration(messageFile));

        /* CONFIG - BOT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        File botSettingFile = new File(getDataFolder(), "bot/config.yml");
        if (!botSettingFile.exists()) saveResource("bot/config.yml", true);
        SettingLoader.loadSettingFile(YamlConfiguration.loadConfiguration(botSettingFile), SettingType.BOT_CONFIG);

        File embedFile = new File(getDataFolder(), "bot/embed.yml");
        if (!embedFile.exists()) saveResource("bot/embed.yml", true);
        EmbedLoader.loadEmbedFile(YamlConfiguration.loadConfiguration(embedFile));

        playerAuthRepository.initialize(new File(getDataFolder(), "authData.yml"));

        /* BOT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        bot = new DiscordAuthBot();

        /* LISTENER
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginManager().registerEvents(new PlayerActionBlockListener(), instance);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);

        /* COMMAND
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginCommand("discord-auth").setExecutor(new DiscordAuthCmd());
        getServer().getPluginCommand("discord-auth").setTabCompleter(new DiscordAuthTab());

        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        TitleScheduler.start(instance);

        /* SUPPORT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (isPluginEnabled("PlaceholderAPI")) new DiscordAuthExpansion().register();
        else System.out.println("PlaceholderAPI 플러그인이 적용되지 않아, 일부기능이 비활성화됩니다.");
    }

    @Override
    public void onDisable() {
        if (bot != null) bot.shutdown();

        SettingContext.getInstance().reset();
        MessageContext.getInstance().reset();
        EmbedContext.getInstance().reset();
        VerifyCodeManager.getInstance().reset();

        TitleScheduler.stop();

        playerAuthRepository.saveAll();
    }

    private boolean isPluginEnabled(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }
}
