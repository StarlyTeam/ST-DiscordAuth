package net.starly.discordauth.bot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.starly.discordauth.bot.manager.RoleManager;
import net.starly.discordauth.context.embed.EmbedContext;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import org.jetbrains.annotations.NotNull;

public class DiscordAuthBtnCmd extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SettingContext settingContext = SettingContext.getInstance();
        EmbedContext embedContext = EmbedContext.getInstance();

        if (event.getName().equals("인증버튼")) {
            if (!event.getMember().getRoles().contains(RoleManager.getInstance().getModeratorRole())) {
                return;
            }


            String verifyBtnStyle = settingContext.get(SettingType.BOT_CONFIG, "button.verifyBtn.style");
            String verifyBtnId = settingContext.get(SettingType.BOT_CONFIG, "button.verifyBtn.id");
            String verifyBtnLabel = settingContext.get(SettingType.BOT_CONFIG, "button.verifyBtn.label");
            Button verifyBtn = Button.of(ButtonStyle.valueOf(verifyBtnStyle.toUpperCase()), verifyBtnId, verifyBtnLabel);

            String customBtnStyle = settingContext.get(SettingType.BOT_CONFIG, "button.customBtn.style");
            String customBtnId = settingContext.get(SettingType.BOT_CONFIG, "button.customBtn.id");
            String customBtnLabel = settingContext.get(SettingType.BOT_CONFIG, "button.customBtn.label");
            Button customBtn = Button.of(ButtonStyle.valueOf(customBtnStyle.toUpperCase()), customBtnId, customBtnLabel);

            MessageEmbed verifyEmbed = embedContext.get("embed.verifyEmbed");
            MessageEmbed successSentEmbed = embedContext.get("embed.successSent");

            event.replyEmbeds(successSentEmbed).setEphemeral(true).queue();
            event.getChannel().sendMessage(
                    new MessageCreateBuilder()
                            .addEmbeds(verifyEmbed)
                            .addComponents(ActionRow.of(verifyBtn, customBtn))
                            .build()
            ).queue();
        }
    }
}
