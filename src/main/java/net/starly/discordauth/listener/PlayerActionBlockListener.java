package net.starly.discordauth.listener;

import net.starly.discordauth.DiscordAuthMain;
import net.starly.discordauth.context.message.MessageContext;
import net.starly.discordauth.context.message.enums.MessageType;
import net.starly.discordauth.context.setting.SettingContext;
import net.starly.discordauth.context.setting.enums.SettingType;
import net.starly.discordauth.repo.PlayerAuthRepository;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerActionBlockListener implements Listener {

    private final PlayerAuthRepository authRepository = DiscordAuthMain.getPlayerAuthRepository();
    private final MessageContext messageContext = MessageContext.getInstance();
    private final SettingContext settingContext = SettingContext.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!settingContext.get(SettingType.CONFIG, "auth.block.interact", Boolean.class)) return;

        Player player = event.getPlayer();
        if (player.isOp() || authRepository.isAuthenticated(player.getUniqueId())) return;


        messageContext.get(MessageType.ERROR, "interactBlocked").send(player);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!settingContext.get(SettingType.CONFIG, "auth.block.command", Boolean.class)) return;

        Player player = event.getPlayer();
        if (player.isOp() || authRepository.isAuthenticated(player.getUniqueId())) return;

        Command discordAuthCommand = DiscordAuthMain.getInstance().getCommand("discord-auth");
        String currentCommand = event.getMessage().split(" ")[0];
        if (!discordAuthCommand.getAliases().contains(currentCommand)) return;


        messageContext.get(MessageType.ERROR, "commandBlocked").send(player);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!settingContext.get(SettingType.CONFIG, "auth.block.move", Boolean.class)) return;

        Player player = event.getPlayer();
        if (player.isOp() || authRepository.isAuthenticated(player.getUniqueId())) return;


        messageContext.get(MessageType.ERROR, "moveBlocked").send(player);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!settingContext.get(SettingType.CONFIG, "auth.block.chat", Boolean.class)) return;

        Player player = event.getPlayer();
        if (player.isOp() || authRepository.isAuthenticated(player.getUniqueId())) return;


        messageContext.get(MessageType.ERROR, "chatBlocked").send(player);
        event.setCancelled(true);
    }
}
