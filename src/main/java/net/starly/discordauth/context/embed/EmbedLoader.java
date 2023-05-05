package net.starly.discordauth.context.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;

public class EmbedLoader {

    public static void loadEmbedFile(FileConfiguration config) {

        EmbedContext embedContext = EmbedContext.getInstance();
        config.getKeys(true).forEach(key -> {
            if (!config.isConfigurationSection(key)) return;

            ConfigurationSection section = config.getConfigurationSection(key);
            EmbedBuilder builder = new EmbedBuilder();

            String title = section.getString("title");
            String description = section.isList("description") ?
                    String.join("\n", section.getStringList("description"))
                    : section.getString("description");
            String thumbnail = section.getString("thumbnail");
            String image = section.getString("image");
            String color = section.getString("color");
            String author = section.getString("author");
            String footer = section.getString("footer");

            if (title != null) builder.setTitle(title);
            if (description != null) builder.setDescription(description);
            if (thumbnail != null) builder.setThumbnail(thumbnail);
            if (image != null) builder.setImage(image);
            if (color != null) builder.setColor(Color.decode(color));
            if (author != null) builder.setAuthor(author);
            if (footer != null) builder.setFooter(footer);


            try {
                embedContext.set(key, builder.build());
            } catch (IllegalStateException ignored) {}
        });
    }
}
