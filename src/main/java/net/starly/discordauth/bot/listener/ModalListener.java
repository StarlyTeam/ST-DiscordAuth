package net.starly.discordauth.bot.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.bot.manager.RoleManager;
import net.starly.discordauth.context.embed.EmbedContext;
import net.starly.discordauth.context.message.MessageContext;
import net.starly.discordauth.context.message.enums.MessageType;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import net.starly.discordauth.data.VerifyCodeManager;
import net.starly.discordauth.repo.PlayerAuthRepository;
import net.starly.discordauth.util.TeleportUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        MessageContext messageContext = MessageContext.getInstance();
        SettingContext settingContext = SettingContext.getInstance();
        EmbedContext embedContext = EmbedContext.getInstance();

        String modalId = event.getModalId();
        if (modalId.equals(settingContext.get(SettingType.BOT_CONFIG, "modal.verifyCode.id"))) {
            VerifyCodeManager codeManager = VerifyCodeManager.getInstance();
            String verifyCode = event.getValue("VERIFY_CODE").getAsString();

            if (!codeManager.has(verifyCode)) {
                event.replyEmbeds(embedContext.get("embed.verifyCodeNotExists")).setEphemeral(true).queue();
                return;
            }

            UUID playerId = codeManager.get(verifyCode);
            Player player = DiscordAuthMain.getInstance().getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                event.replyEmbeds(embedContext.get("embed.playerIsOffline")).setEphemeral(true).queue();
                return;
            }

            PlayerAuthRepository authRepository = DiscordAuthMain.getPlayerAuthRepository();
            if (authRepository.getDiscordId(playerId) != null) {
                event.replyEmbeds(embedContext.get("embed.alreadyAuthenticatedPlayer")).setEphemeral(true).queue();
                return;
            }


            User discordUser = event.getInteraction().getUser();
            authRepository.setDiscordId(playerId, discordUser.getId());

            event.replyEmbeds(embedContext.get("embed.authComplete")).setEphemeral(true).queue();
            messageContext.get(MessageType.NORMAL, "authComplete", msg ->
                            msg
                                    .replace("{discordId}", discordUser.getId())
                                    .replace("{discordTag}", discordUser.getAsTag())
                                    .replace("{playerId}", String.valueOf(player.getUniqueId()))
                                    .replace("{playerName}", player.getDisplayName()))
                    .send(player);


            try {
                RoleManager roleManager = RoleManager.getInstance();
                roleManager.getGuild().addRoleToMember(discordUser, roleManager.getUserRole()).queue();
                roleManager.getGuild().modifyNickname(roleManager.getGuild().getMemberById(discordUser.getId()),
                        settingContext.get(SettingType.CONFIG, "auth.changeNickname")
                                .replace("{discordTag}", discordUser.getAsTag())
                                .replace("{playerName}", player.getDisplayName())
                                .replace("{playerId}", String.valueOf(player.getUniqueId())));
            } catch (HierarchyException ignored) {
                DiscordAuthMain.getInstance().getLogger().warning("서버 주인 또는 봇보다 높은 권한을 가진 멤버의 역할은 수정 할 수 없습니다.");
            }


            VerifyCodeManager.getInstance().remove(verifyCode);


            TeleportUtil.teleport(TeleportUtil.LobbyType.LOBBY, player);
        }
    }
}
