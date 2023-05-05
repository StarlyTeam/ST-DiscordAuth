package net.starly.discordauth.context.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;

/*
* Code From: 동욱 & 대영
* Migrated By: 예준
*/
@AllArgsConstructor
public class STMessage {

    @Getter private final String prefix;
    @Getter private final String message;

    public String getText() {
        return prefix + message;
    }

    public void send(CommandSender target) {
        if (message.isEmpty()) return;
        target.sendMessage(prefix + message);
    }
}
