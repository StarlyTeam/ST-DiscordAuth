package net.starly.discordauth.bot.manager;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.bot.DiscordAuthBot;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;

public class RoleManager {

    private static RoleManager instance;
    public static RoleManager getInstance() {
        if (instance == null) instance = new RoleManager();
        return instance;
    }

    private RoleManager() {
        loadConfig();
    }


    @Getter private Guild guild;
    @Getter private Role moderatorRole;
    @Getter private Role userRole;


    public void loadConfig() {
        this.guild = null;
        this.moderatorRole = this.userRole = null;

        JDA jda = DiscordAuthBot.getJda();
        SettingContext settingContext = SettingContext.getInstance();

        this.guild = jda.getGuildById(settingContext.get(SettingType.BOT_CONFIG, "guildId"));
        if (guild == null) DiscordAuthMain.getInstance().getLogger().severe("길드를 찾을 수 없습니다. 길드 ID를 다시 확인해주세요.");
        else {
            try {
                this.moderatorRole = guild.getRoleById(settingContext.get(SettingType.BOT_CONFIG, "role.moderatorRoleId"));
                this.userRole = guild.getRoleById(settingContext.get(SettingType.BOT_CONFIG, "role.userRoleId"));
            } catch (Exception ex) {
                DiscordAuthMain.getInstance().getLogger().severe("역할을 찾을 수 없습니다. 역할 ID를 다시 확인해주세요.");
                ex.printStackTrace();
            }
        }
    }
}