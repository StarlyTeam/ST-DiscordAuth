package net.starly.discordauth.command;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.AuthCodeMapData;
import net.starly.discordauth.data.PlayerAuthData;
import net.starly.discordauth.event.PlayerJoinListener;
import net.starly.discordauth.event.PlayerMoveListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import static net.starly.discordauth.DiscordAuthMain.messageConfig;

public class DiscordAuthCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageConfig.getMessage("others.cannot_execute_in_console"));
            return true;
        }
        Player player = (Player) sender;

        PlayerAuthData data = new PlayerAuthData(player);
        if (args.length == 0) {
            if (!player.hasPermission("starly.discordauth.status")) {
                player.sendMessage(messageConfig.getMessage("others.no_permission"));
                return true;
            }

            if (!data.isAuthenticated()) messageConfig.getMessages("messages.not_authenticated").forEach(line -> player.sendMessage(line
                    .replace("{player}", player.getDisplayName())));
            else messageConfig.getMessages("messages.authenticated").forEach(line -> player.sendMessage(line
                    .replace("{player}", player.getDisplayName())));

            return true;
        } else if (args.length == 1) {
            if (Arrays.asList("리로드", "reload").contains(args[0].toLowerCase())) {
                if (!player.hasPermission("starly.discordauth.reload")) {
                    player.sendMessage(messageConfig.getMessage("others.no_permission"));
                    return true;
                }

                // RELOAD CONFIG
                DiscordAuthMain.config.loadDefaultConfig();
                DiscordAuthMain.botConfig.loadDefaultConfig();

                // RELOAD BOT
                if (DiscordAuthMain.bot != null) DiscordAuthMain.bot.reloadBot(player);

                // RELOAD EVENT HANDLERS
                HandlerList.unregisterAll(DiscordAuthMain.getPlugin());
                Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), DiscordAuthMain.getPlugin());
                if (DiscordAuthMain.config.getBoolean("other_settings.enable_cancellation_move"))
                    Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), DiscordAuthMain.getPlugin());

                // RELOAD MESSAGE CONFIG
                DiscordAuthMain.messageConfig.reloadConfig();


                player.sendMessage(messageConfig.getMessage("others.reloaded_config"));
                return true;
            } else if (Arrays.asList("발급", "코드", "generate", "code").contains(args[0].toLowerCase())) {
                if (!player.hasPermission("starly.discordauth.gencode")) {
                    player.sendMessage(messageConfig.getMessage("others.no_permission"));
                    return true;
                }

                if (data.isAuthenticated()) {
                    player.sendMessage(messageConfig.getMessage("messages.already_authenticated"));
                    return true;
                }

                if (AuthCodeMapData.authCodeMap.containsValue(player)) {
                    player.sendMessage(messageConfig.getMessage("messages.already_requested"));
                    return true;
                }

                String code;
                do {
                    code = String.valueOf(Math.round(Math.random() * 1000000));
                } while (code.length() != 6);

                AuthCodeMapData.authCodeMap.put(code, player);

                long sec = DiscordAuthMain.config.getInt("others.code_expire_time");
                String finalCode = code;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (AuthCodeMapData.authCodeMap.containsKey(finalCode)) {
                            AuthCodeMapData.authCodeMap.get(finalCode).sendMessage(messageConfig.getMessage("messages.code_expired"));

                            AuthCodeMapData.authCodeMap.remove(finalCode);
                        }
                    }
                }.runTaskLaterAsynchronously(DiscordAuthMain.getPlugin(), sec * 20L);


                String finalCode1 = code;
                messageConfig.getMessages("messages.verify_code_generated").forEach(line -> player.sendMessage(line
                        .replace("{code}", finalCode1)
                        .replace("{remain_time}", String.valueOf(sec))));
                return true;
            } else if (Arrays.asList("해제", "unauth").contains(args[0].toLowerCase())) {
                if (args.length == 1) {
                    if (!player.hasPermission("starly.discordauth.unauth.self")) {
                        player.sendMessage(messageConfig.getMessage("others.no_permission"));
                        return true;
                    }

                    data.setAuthenticated(false);
                    data.setDiscordId("");
                    player.sendMessage(messageConfig.getMessage("messages.unauthorized"));
                    return true;
                } else if (args.length == 2) {
                    if (!player.hasPermission("starly.discordauth.unauth.other")) {
                        player.sendMessage(messageConfig.getMessage("others.no_permission"));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(messageConfig.getMessage("others.player_not_found"));
                        return true;
                    }

                    PlayerAuthData targetData = new PlayerAuthData(target);

                    targetData.setAuthenticated(false);
                    targetData.setDiscordId("");
                    player.sendMessage(messageConfig.getMessage("messages.unauthorized"));
                    return true;
                }
            }
        }

        player.sendMessage(messageConfig.getMessage("others.wrong_command"));
        return false;
    }
}
