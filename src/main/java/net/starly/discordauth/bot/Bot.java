package net.starly.discordauth.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.bot.listener.DiscordListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Bot {
    private final Logger log = Bukkit.getLogger();
    private JDA jda;
    private JavaPlugin plugin;
    private String token;

    public Bot(JavaPlugin plugin, String token) {
        this.plugin = plugin;
        this.token = token;

        initJDA();
    }

    public JDA getJDA() {
        return jda;
    }

    public void reloadBot(Player player) {
        if (jda == null) {
            player.sendMessage("성공적으로 콘피그 및 봇을 리로드하는중 오류가 발생했습니다!");
            return;
        }

        DiscordAuthMain.botConfig.reloadConfig();

        loadCommands();
        loadStatus();
    }

    private void initJDA() {
        if (token.equals("YOUR_TOKEN")) {
            log.warning("봇 토큰을 설정해주세요!");
            disableIt();
            return;
        } else if (DiscordAuthMain.botConfig.getString("bot_settings.richPresence.name").equals("YOUR_STATUS")) {
            log.warning("봇 상태 메세지를 설정해주세요.");
            disableIt();
            return;
        } else if (DiscordAuthMain.botConfig.getString("bot_settings.roles.verifiedRole").equals("YOUR_ROLE_ID")) {
            log.warning("인증 완료시 지급될 역할 ID를 설정해주세요.");
            disableIt();
            return;
        } else if (DiscordAuthMain.botConfig.getString("bot_settings.guildId").equals("YOUR_ROLE_ID")) {
            log.warning("봇이 작동할 서버 ID를 설정해주세요.");
            disableIt();
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)

                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES)

                    .setEnableShutdownHook(false)

                    .addEventListeners(new DiscordListener())

                    .build();

            jda.awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
            log.warning("봇 로딩중에 오류가 발생했습니다!");
            disableIt();
            return;
        }

        loadCommands();
        loadStatus();
    }

    private void loadStatus() {
        OnlineStatus st = OnlineStatus.ONLINE; //기본값
        switch (DiscordAuthMain.botConfig.getString("bot_settings.status")) {
            case "online" -> st = OnlineStatus.ONLINE;
            case "idle" -> st = OnlineStatus.IDLE;
            case "dnd" -> st = OnlineStatus.DO_NOT_DISTURB;
            default -> {
                log.warning("봇 상태 설정중에 오류가 발생했습니다!");
                disableIt();
                return;
            }
        }

        Activity.ActivityType act = Activity.ActivityType.PLAYING; //기본값
        switch (DiscordAuthMain.botConfig.getString("bot_settings.richPresence.type")) {
            case "playing" -> act = Activity.ActivityType.PLAYING;
            case "watching" -> act = Activity.ActivityType.WATCHING;
            case "listening" -> act = Activity.ActivityType.LISTENING;
            default -> {
                log.warning("봇 상태 설정중에 오류가 발생했습니다!");
                disableIt();
                return;
            }
        }

        Activity ac = Activity.of(act, DiscordAuthMain.botConfig.getString("bot_settings.richPresence.name"));

        jda.getPresence().setPresence(ac, false);
        jda.getPresence().setStatus(st);
    }

    private void loadCommands() {
        log.info("(/) 명령어를 업데이트 합니다!");
        jda.updateCommands().addCommands(
                Commands.slash("연동생성", "마인크래프트 계정 연동버튼 생성 명령어입니다.")
        ).queue();
    }

    private void disableIt() {
        log.info("플러그인을 비활성화합니다.");
        Bukkit.getPluginManager().disablePlugin(plugin);
    }
}
