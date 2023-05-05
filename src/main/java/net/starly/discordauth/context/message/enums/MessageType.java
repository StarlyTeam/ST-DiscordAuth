package net.starly.discordauth.context.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * Code From: 동욱 & 대영
 * Migrated By: 예준
 */
@AllArgsConstructor
public enum MessageType {
    NORMAL("messages"),
    ERROR("errorMessages");

    @Getter
    private final String path;
}
