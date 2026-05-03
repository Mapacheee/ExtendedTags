package me.mapacheee.extendedtags.hook;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.extendedtags.ExtendedTagsPlugin;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.service.PlayerTagService;
import me.mapacheee.extendedtags.service.TagService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.UUID;

@Service
public final class PlaceholderHook extends PlaceholderExpansion {

    private TagService tagService;
    private PlayerTagService playerTagService;
    private final Logger logger;
    private boolean enabled = false;

    @Inject
    public PlaceholderHook(Logger logger) {
        this.logger = logger;
    }

    @OnEnable
    public void onEnable() {
        try {
            tagService = ExtendedTagsPlugin.getService(TagService.class);
            playerTagService = ExtendedTagsPlugin.getService(PlayerTagService.class);

            if (tagService == null || playerTagService == null) {
                logger.warn("TagService or PlayerTagService not available yet");
                return;
            }

            ExtendedTagsPlugin plugin = ExtendedTagsPlugin.getInstance();
            if (plugin == null) {
                logger.warn("ExtendedTagsPlugin instance is null");
                return;
            }

            var papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (papi == null) {
                logger.warn("PlaceholderAPI not found");
                return;
            }

            this.enabled = true;
            this.register();
            logger.info("PlaceholderAPI expansion registered for ExtendedTags.");
        } catch (Exception e) {
            logger.error("Failed to register PlaceholderAPI expansion", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "extendedtags";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Mapacheee";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(Player player, @NonNull String params) {
        if (!params.equalsIgnoreCase("tag")) {
            return null;
        }
        if (player == null) {
            return null;
        }
        try {
            UUID uuid = player.getUniqueId();
            String equippedKey = playerTagService.getEquippedTag(uuid);
            if (equippedKey != null && !equippedKey.isEmpty()) {
                Tag tag = tagService.getTag(equippedKey);
                return tag != null ? tag.getName() : "";
            }
        } catch (Exception e) {
            logger.warn("Error getting tag for player {}: {}", player.getName(), e.getMessage());
        }
        return "";
    }
}