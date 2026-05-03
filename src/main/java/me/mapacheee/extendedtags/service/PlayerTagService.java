package me.mapacheee.extendedtags.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedtags.data.PlayerTagData;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.data.TagStorage;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.UUID;

@Service
public final class PlayerTagService {

    private final TagStorage tagStorage;
    private final TagService tagService;
    private final Logger logger;

    @Inject
    public PlayerTagService(TagStorage tagStorage, TagService tagService, Logger logger) {
        this.tagStorage = tagStorage;
        this.tagService = tagService;
        this.logger = logger;
    }

    public PlayerTagData getOrCreate(Player player) {
        return tagStorage.getOrCreatePlayerData(player.getUniqueId(), player.getName());
    }

    public String getEquippedTag(UUID uuid) {
        PlayerTagData data = tagStorage.getPlayerData(uuid);
        if (data == null || data.getEquippedTag() == null || data.getEquippedTag().isEmpty()) {
            return null;
        }
        return data.getEquippedTag();
    }

    public boolean setEquippedTag(Player player, String tagKey) {
        PlayerTagData data = getOrCreate(player);

        if (tagKey == null || "none".equalsIgnoreCase(tagKey)) {
            data.setEquippedTag("");
            tagStorage.savePlayerData(data);
            return true;
        }

        Tag tag = tagService.getTag(tagKey);
        if (tag == null || !tag.isEnabled()) {
            return false;
        }

        if (tag.isRequiresPurchase() && !data.hasTag(tagKey)) {
            return false;
        }

        if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
            return false;
        }

        data.setEquippedTag(tagKey.toLowerCase());
        tagStorage.savePlayerData(data);
        return true;
    }

    public boolean hasTag(UUID uuid, String tagKey) {
        PlayerTagData data = tagStorage.getPlayerData(uuid);
        return data != null && data.hasTag(tagKey);
    }

    public void grantTag(UUID uuid, String playerName, String tagKey) {
        PlayerTagData data = tagStorage.getOrCreatePlayerData(uuid, playerName);
        data.addTag(tagKey);
        tagStorage.savePlayerData(data);
    }

    public void revokeTag(UUID uuid, String playerName, String tagKey) {
        PlayerTagData data = tagStorage.getOrCreatePlayerData(uuid, playerName);
        data.removeTag(tagKey);
        if (tagKey.equalsIgnoreCase(data.getEquippedTag())) {
            data.setEquippedTag("");
        }
        tagStorage.savePlayerData(data);
    }

    public void preload(Player player) {
        tagStorage.getOrCreatePlayerData(player.getUniqueId(), player.getName());
    }
}