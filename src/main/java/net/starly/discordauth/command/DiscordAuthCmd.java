package net.starly.discordauth.command;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.data.AuthCodeMapData;
import net.starly.discordauth.data.PlayerAuthData;
import net.starly.discordauth.event.PlayerAsyncChatListener;
import net.starly.discordauth.event.PlayerCommandPreprocessListener;
import net.starly.discordauth.event.PlayerJoinListener;
import net.starly.discordauth.event.PlayerMoveListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.starly.discordauth.DiscordAuthMain.messageConfig;

public class DiscordAuthCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageConfig.getMessage("others.cannot_execute_in_console"));
            return true;
        }

        PlayerAuthData data = new PlayerAuthData(player);
        if (args.length == 0) {
            if (!data.isAuthenticated()) messageConfig.getMessages("messages.not_authenticated", Map.of("{player}", player.getDisplayName())).forEach(player::sendMessage);
            else messageConfig.getMessages("messages.authenticated", Map.of("{player}", player.getDisplayName())).forEach(player::sendMessage);

            return true;
        } else if (args.length == 1) {
            if (List.of("리로드", "reload").contains(args[0].toLowerCase())) {
                if (!player.isOp()) {
                    player.sendMessage(messageConfig.getMessage("others.op_command"));
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
            } else if (List.of("발급", "코드", "generate", "code").contains(args[0].toLowerCase())) {
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


                messageConfig.getMessages("messages.verify_code_generated", Map.of("{code}", code, "{remain_time}", String.valueOf(sec))).forEach(player::sendMessage);
                return true;
            }
        } else {
            player.sendMessage(messageConfig.getMessage("messages.wrong_command"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return List.of("리로드", "발급");

        return Collections.emptyList();
    }
}
