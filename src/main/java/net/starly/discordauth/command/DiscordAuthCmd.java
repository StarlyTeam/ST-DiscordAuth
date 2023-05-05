package net.starly.discordauth.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.bot.DiscordAuthBot;
import net.starly.discordauth.bot.manager.RoleManager;
import net.starly.discordauth.context.embed.EmbedContext;
import net.starly.discordauth.context.embed.EmbedLoader;
import net.starly.discordauth.context.message.MessageContext;
import net.starly.discordauth.context.message.MessageLoader;
import net.starly.discordauth.context.message.enums.MessageType;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.SettingLoader;
import net.starly.discordauth.context.setting.enums.SettingType;
import net.starly.discordauth.data.VerifyCodeManager;
import net.starly.discordauth.repo.PlayerAuthRepository;
import net.starly.discordauth.util.TeleportUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordAuthCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        PlayerAuthRepository authRepository = DiscordAuthMain.getPlayerAuthRepository();
        MessageContext messageContext = MessageContext.getInstance();

        if (args.length == 0) {
            if (!player.hasPermission("starly.discordauth.status")) {
                messageContext.get(MessageType.ERROR, "noPermission").send(player);
                return true;
            }

            if (authRepository.isAuthenticated(player.getUniqueId())) {
                User discordUser = DiscordAuthBot.getJda().getUserById(authRepository.getDiscordId(player.getUniqueId()));
                messageContext.get(MessageType.NORMAL, "authTrue", msg -> msg
                                .replace("{discordId}", discordUser.getId())
                                .replace("{discordTag}", discordUser.getAsTag())
                                .replace("{playerId}", String.valueOf(player.getUniqueId()))
                                .replace("{playerName}", player.getDisplayName()))
                        .send(player);
            } else {
                messageContext.get(MessageType.NORMAL, "authFalse", msg -> msg
                                .replace("{playerId}", String.valueOf(player.getUniqueId()))
                                .replace("{playerName}", player.getDisplayName()))
                        .send(player);
            }
            return true;
        }

        switch (args[0]) {
            case "발급": {
                if (!player.hasPermission("starly.discordauth.gencode")) {
                    messageContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                } else if (args.length != 1) {
                    messageContext.get(MessageType.ERROR, "wrongCommand").send(player);
                    return true;
                }

                String verifyCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
                int expireSec = SettingContext.getInstance().get(SettingType.CONFIG, "auth.codeExpirationTime", Integer.class);

                VerifyCodeManager codeManager = VerifyCodeManager.getInstance();
                if (codeManager.has(player.getUniqueId())) {
                    messageContext.get(MessageType.ERROR, "alreadyHaveAuthCode").send(player);
                    return true;
                }
                codeManager.set(verifyCode, player.getUniqueId());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        VerifyCodeManager codeManager = VerifyCodeManager.getInstance();
                        if (codeManager.has(verifyCode)) {
                            codeManager.remove(verifyCode);
                            messageContext.get(MessageType.NORMAL, "authCodeExpired").send(player);
                        }
                    }
                }.runTaskLater(DiscordAuthMain.getInstance(), expireSec * 20L);

                messageContext.get(MessageType.NORMAL, "authCodeCreated", msg -> msg
                                .replace("{authCode}", verifyCode)
                                .replace("{expireSec}", String.valueOf(expireSec)))
                        .send(player);
                return true;
            }

            case "해제": {
                Player target;
                if (args.length == 1) {
                    if (!player.hasPermission("starly.discordauth.unauth.self")) {
                        messageContext.get(MessageType.ERROR, "noPermission").send(player);
                        return true;
                    }

                    target = player;
                } else if (args.length == 2) {
                    if (!player.hasPermission("starly.discordauth.unauth.other")) {
                        messageContext.get(MessageType.ERROR, "noPermission").send(player);
                        return true;
                    }

                    target = DiscordAuthMain.getInstance().getServer().getPlayer(args[1]);
                    if (target == null || !target.isOnline()) {
                        messageContext.get(MessageType.ERROR, "playerNotFound").send(player);
                        return true;
                    }
                } else {
                    messageContext.get(MessageType.ERROR, "wrongCommand").send(player);
                    return true;
                }

                String discordId = authRepository.getDiscordId(target.getUniqueId());
                if (discordId == null) {
                    messageContext.get(MessageType.NORMAL, "authFalse").send(player);
                    return true;
                }


                authRepository.setDiscordId(player.getUniqueId(), null);


                try {
                    RoleManager roleManager = RoleManager.getInstance();
                    roleManager.getGuild().removeRoleFromMember(roleManager.getGuild().getMemberById(discordId), roleManager.getUserRole()).queue();
                } catch (HierarchyException ignored) {
                    DiscordAuthMain.getInstance().getLogger().warning("서버 주인 또는 봇보다 높은 권한을 가진 멤버의 역할은 수정 할 수 없습니다.");
                } catch (NullPointerException ignored) {}


                TeleportUtil.teleport(TeleportUtil.LobbyType.VERIFY_LOBBY, target);
                return true;
            }

            case "리로드": {
               if (!player.hasPermission("starly.discordauth.reload")) {
                   messageContext.get(MessageType.ERROR, "noPermission").send(player);
                   return true;
               } else if (args.length != 1) {
                    messageContext.get(MessageType.ERROR, "wrongCommand").send(player);
                    return true;
                }

                File dataFolder = DiscordAuthMain.getInstance().getDataFolder();

                SettingContext.getInstance().reset();
                SettingLoader.loadSettingFile(YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml")), SettingType.CONFIG);
                SettingLoader.loadSettingFile(YamlConfiguration.loadConfiguration(new File(dataFolder, "bot/config.yml")), SettingType.BOT_CONFIG);

                MessageContext.getInstance().reset();
                MessageLoader.loadMessageFile(YamlConfiguration.loadConfiguration(new File(dataFolder, "message.yml")));

                EmbedContext.getInstance().reset();
                EmbedLoader.loadEmbedFile(YamlConfiguration.loadConfiguration(new File(dataFolder, "bot/embed.yml")));

                DiscordAuthMain.getInstance().reloadConfig();

                RoleManager.getInstance().loadConfig();


                DiscordAuthBot bot = DiscordAuthMain.getInstance().getBot();
                bot.shutdown();
                bot.start();


                messageContext.get(MessageType.NORMAL, "reloadComplete").send(player);
                return true;
            }

            default: {
                return true;
            }
        }
    }
}
