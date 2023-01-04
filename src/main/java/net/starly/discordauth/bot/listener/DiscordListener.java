package net.starly.discordauth.bot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.AuthCodeMapData;
import net.starly.discordauth.data.PlayerAuthData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;

import static net.starly.discordauth.DiscordAuthMain.botConfig;
import static net.starly.discordauth.DiscordAuthMain.config;

public class DiscordListener implements EventListener {
    private Color embedColor;

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            embedColor = new Color(
                    Integer.parseInt(botConfig.getString("bot_messages.embedColor").substring(1, 3), 16),
                    Integer.parseInt(botConfig.getString("bot_messages.embedColor").substring(3, 5), 16),
                    Integer.parseInt(botConfig.getString("bot_messages.embedColor").substring(5, 7), 16)
            );
        } else if (event instanceof SlashCommandInteractionEvent e) {
            if (e.getName().equalsIgnoreCase("연동생성")) {
                if (!e.isFromGuild()) {
                    e.replyEmbeds(getEmbed("onlyGuild")).setEphemeral(true).queue();
                    return;
                }
                if (!isModerator(e.getGuild(), e.getUser())) return;

                MessageEmbed embed = getEmbed("buttonEmbed");

                Button btn1__discordauth = Button.primary("btn1__discordauth", botConfig.getString("bot_messages.buttonEmbed.btn1.label"));
                Button btn2__discordauth = Button.secondary("btn2__discordauth", botConfig.getString("bot_messages.buttonEmbed.btn2.label"));
                Button btn3__discordauth = Button.secondary("btn3__discordauth", botConfig.getString("bot_messages.buttonEmbed.btn3.label"));

                MessageCreateData message = new MessageCreateBuilder()
                        .addEmbeds(embed)
                        .addComponents(ActionRow.of(btn1__discordauth, btn2__discordauth, btn3__discordauth))
                        .build();

                e.reply("인증 임베드를 생성하였습니다!").setEphemeral(true).queue();
                e.getChannel().sendMessage(message).queue();
            }
        } else if (event instanceof ButtonInteractionEvent e) {
            if (e.getButton().getId().equals("btn1__discordauth")) {
                TextInput verifyCodeInput = TextInput.create("verifyCode__discordauth", "인증코드", TextInputStyle.SHORT)
                        .setPlaceholder("서버에서 발급받은 코드를 입력해주세요.")
                        .setMinLength(6)
                        .setMaxLength(6)
                        .build();

                Modal modal = Modal.create("verifyCode__discordauth", "디스코드 연동하기")
                        .addActionRows(ActionRow.of(verifyCodeInput))
                        .build();

                e.replyModal(modal).queue();
            } else if (e.getButton().getId().equals("btn2__discordauth")) {
                e.replyEmbeds(getEmbed("btn2")).setEphemeral(true).queue();
            } else if (e.getButton().getId().equals("btn3__discordauth")) {
                e.replyEmbeds(getEmbed("btn3")).setEphemeral(true).queue();
            }
        } else if (event instanceof ModalInteractionEvent e) {
            if (e.getModalId().equals("verifyCode__discordauth")) {
                //입력받은 코드
                String currentCode = e.getValue("verifyCode__discordauth").getAsString();


                //6자리인지 확인
                if (currentCode.length() != 6) {
                    e.replyEmbeds(getEmbed("only6Characters")).setEphemeral(true).queue();
                    return;
                }


                Player player = null;
                //존재하는 코드인지 확인
                try {
                    if (!AuthCodeMapData.authCodeMap.containsKey(currentCode)) {
                        e.replyEmbeds(getEmbed("notAvailableCode")).setEphemeral(true).queue();
                        return;
                    }

                    player = AuthCodeMapData.authCodeMap.get(currentCode);
                    if (player == null) return;

                    if (player.isOnline()) {
                        player.getPlayer().sendMessage(config.getMessage("messages.connected", Map.of("{tag}", e.getUser().getAsTag())));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                //코드 삭제
                AuthCodeMapData.authCodeMap.remove(currentCode);


                //인증 처리
                PlayerAuthData data = new PlayerAuthData(player);
                data.setDiscordId(e.getUser().getId());
                data.setAuthenticated(true);
                data.saveConfig();


                //성공 임베드 전송
                ReplyCallbackAction replyCallbackAction = e.deferReply();
                InteractionHook interactionHook = replyCallbackAction.setEphemeral(true)
                        .addEmbeds(getEmbed("successVerify"))
                        .complete();

                //역할 지급
                try {
                    Guild guild = e.getJDA().getGuildById(botConfig.getString("bot_settings.guildId"));
                    if (guild == null) {
                        interactionHook.editOriginalEmbeds(getEmbed("notAvailableGuildId")).queue();
                        return;
                    }

                    Member member = guild.getMember(e.getUser());
                    if (member == null) {
                        interactionHook.editOriginalEmbeds(getEmbed("notGuildMember")).queue();
                        return;
                    }

                    Role role = guild.getRoleById(botConfig.getString("bot_settings.roles.verifiedRole"));
                    if (role == null) {
                        interactionHook.editOriginalEmbeds(getEmbed("notAvailableRoleId")).queue();
                        return;
                    }

                    guild.addRoleToMember(member, role).queue();
                    interactionHook.editOriginalEmbeds(getEmbed("successGiveRole")).queue();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                //닉네임 변경
                if (botConfig.getBoolean("bot_settings.changeDiscordName.enable")) {
                    try {
                        String nickname = botConfig.getString("bot_settings.changeDiscordName.name")
                                .replace("%minecraft_name%", player.getName())
                                .replace("%discord_name%", e.getUser().getName())
                                .replace("%discord_tag%",
                                        e.getUser().getAsTag().substring(e.getUser().getAsTag().length() - 4))
                                .replace("%discord_full%", e.getUser().getAsTag());

                        Guild guild = DiscordAuthMain.bot.getJDA().getGuildById(botConfig.getString("bot_settings.guildId"));
                        guild.getMember(e.getUser()).modifyNickname(nickname).queue();
                    } catch (Exception ex) {
                        if (ex instanceof HierarchyException) {
                            interactionHook.editOriginalEmbeds(getEmbed("failedModifyNick")).queue();
                        } else {
                            ex.printStackTrace();
                        }
                    }
                }


                //로비로 이동
                if (DiscordAuthMain.config.getBoolean("location.lobby.enable")) {
                    Player finalPlayer = player;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            finalPlayer.teleport(DiscordAuthMain.config.getLocation("location.lobby.location"));
                        }
                    }.runTask(DiscordAuthMain.getPlugin());

                    try {
                        DiscordAuthMain.config.getMessages("location.lobby.message").forEach(player::sendMessage);
                    } catch (NoSuchMethodError ex) {
                        Bukkit.getLogger().info("§9[§eDiscordAuth§9] §eST-Core §c플러그인의 버전이 너무 낮습니다. 1.4.1 이상의 버전으로 업데이트 해주세요.");
                        Bukkit.getLogger().info("§9[§eDiscordAuth§9] §eST-Core §f플러그인 다운로드 : §7https://discord.gg/TF8jqSJjCG");
                    }
                }

                //관리자 알림 메세지 전송
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.isOp()) return;

                    onlinePlayer.sendMessage(DiscordAuthMain.config.getMessage("messages.connected_op", Map.of(
                            "{player}", player.getDisplayName(),
                            "{tag}", e.getUser().getAsTag())));
                }
            }
        }
    }

    private MessageEmbed getEmbed(@NotNull String key) {
        try {
            return new EmbedBuilder()
                    .setColor(embedColor)
                    .setTitle(botConfig.getString("bot_messages." + key + ".title"))
                    .setDescription(botConfig.getString("bot_messages." + key + ".description"))
                    .setThumbnail(botConfig.getString("bot_messages." + key + ".thumbnailURL"))
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private Boolean isModerator(Guild guild, User user) {
        Role role = guild.getRoleById(botConfig.getString("bot_settings.roles.moderatorRole"));
        if (role == null) {
            Bukkit.getLogger().warning("역할이 존재하지 않습니다. | (config.yml && \"bot_settings.roles.moderatorRole\") 역할 ID: " + botConfig.getString("bot_settings.roles.moderatorRole"));
            return false;
        }

        if (guild.getMemberById(user.getId()).getRoles().contains(role)) return true;
        return false;
    }
}