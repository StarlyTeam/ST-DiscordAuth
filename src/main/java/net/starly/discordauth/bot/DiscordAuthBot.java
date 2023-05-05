package net.starly.discordauth.bot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.bot.command.AuthBtnCmd;
import net.starly.discordauth.bot.listener.BtnListener;
import net.starly.discordauth.bot.listener.ModalListener;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;

public class DiscordAuthBot {

    @Getter private static JDA jda;

    public DiscordAuthBot() {
        start();
    }

    private Activity getActivity() {
        SettingContext settingContext = SettingContext.getInstance();
        Activity.ActivityType activity = Activity.ActivityType.valueOf(settingContext.get(SettingType.BOT_CONFIG, "presence.activity.type").toUpperCase());
        return Activity.of(activity, settingContext.get(SettingType.BOT_CONFIG, "presence.activity.text"));
    }

    private OnlineStatus getOnlineStatus() {
        return OnlineStatus.fromKey(SettingContext.getInstance().get(SettingType.BOT_CONFIG, "presence.status").toLowerCase());
    }

    private void loadSlashCommands() {
        DiscordAuthMain.getInstance().getLogger().info("(/) 명령어를 업데이트합니다.");
        jda.updateCommands().addCommands(Commands.slash("인증버튼", "인증버튼을 생성합니다.")).queue();
    }


    public void start() {
        SettingContext settingContext = SettingContext.getInstance();
        if (!settingContext.get(SettingType.BOT_CONFIG, "enable", Boolean.class)) return;

        try {
            JDABuilder builder = JDABuilder.createDefault(settingContext.get(SettingType.BOT_CONFIG, "token"));
            jda = builder
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES)
                    .setEnableShutdownHook(false)

                    .addEventListeners(new AuthBtnCmd())
                    .addEventListeners(new BtnListener())
                    .addEventListeners(new ModalListener())

                    .setActivity(getActivity())
                    .setStatus(getOnlineStatus())

                    .build();
            jda.awaitReady();
        } catch (IllegalArgumentException ex) {
            DiscordAuthMain.getInstance().getLogger().severe("토큰이 잘못되었습니다. 봇 토큰을 다시 확인해주세요.");
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        loadSlashCommands();
    }

    public void shutdown() {
        if (jda != null) {
            try {
                jda.shutdown();
            } catch (NoClassDefFoundError ignored) {
            }
        }
    }
}
