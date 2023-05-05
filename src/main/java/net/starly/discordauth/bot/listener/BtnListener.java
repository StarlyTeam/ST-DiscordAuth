package net.starly.discordauth.bot.listener;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.starly.discordauth.context.embed.EmbedContext;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import org.jetbrains.annotations.NotNull;

public class BtnListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        SettingContext settingContext = SettingContext.getInstance();

        String buttonId = event.getButton().getId();
        if (buttonId.equals(settingContext.get(SettingType.BOT_CONFIG, "button.verifyBtn.id"))) {
            TextInput verifyCodeInput = TextInput
                    .create("VERIFY_CODE", "인증코드", TextInputStyle.SHORT)
                    .setRequiredRange(6, 6)
                    .setRequired(true)
                    .setPlaceholder("123456")
                    .build();

            Modal verifyCodeModal = Modal
                    .create(
                            settingContext.get(SettingType.BOT_CONFIG, "modal.verifyCode.id"),
                            settingContext.get(SettingType.BOT_CONFIG, "modal.verifyCode.title")
                    )
                    .addActionRow(verifyCodeInput)
                    .build();

            event.replyModal(verifyCodeModal).queue();
        } else if (buttonId.equals(settingContext.get(SettingType.BOT_CONFIG, "button.customBtn.id"))) {
            MessageEmbed embed = EmbedContext.getInstance().get(settingContext.get(SettingType.BOT_CONFIG, "button.customBtn.embed"));
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }
}
