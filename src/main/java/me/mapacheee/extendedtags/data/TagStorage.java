package me.mapacheee.extendedtags.data;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.extendedtags.ExtendedTagsPlugin;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class TagStorage {

    private final Logger logger;
    private final File tagsFile;
    private final File playerDataFile;
    private YamlConfigurationLoader tagsLoader;
    private YamlConfigurationLoader playerDataLoader;
    private ConfigurationNode tagsNode;
    private ConfigurationNode playerDataNode;

    private final Map<String, Tag> tags = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerTagData> playerData = new ConcurrentHashMap<>();

    @Inject
    public TagStorage(Logger logger) {
        this.logger = logger;
        ExtendedTagsPlugin plugin = ExtendedTagsPlugin.getInstance();
        this.tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    @OnEnable
    public void onEnable() {
        try {
            ExtendedTagsPlugin plugin = ExtendedTagsPlugin.getInstance();
            plugin.saveDefaultConfig();

            if (!tagsFile.exists()) {
                plugin.saveResource("tags.yml", false);
            }

            if (!playerDataFile.exists()) playerDataFile.createNewFile();

            tagsLoader = YamlConfigurationLoader.builder().file(tagsFile).build();
            playerDataLoader = YamlConfigurationLoader.builder().file(playerDataFile).build();

            if (!playerDataFile.exists()) playerDataFile.createNewFile();

            tagsNode = tagsLoader.load();
            playerDataNode = playerDataLoader.load();

            loadTags();
            loadPlayerData();
            migrateOldPlayerDataFormat();

            logger.info("Loaded {} tags and {} player data entries", tags.size(), playerData.size());
        } catch (Exception e) {
            logger.error("Failed to initialize tag storage", e);
        }
    }

    private void loadTags() {
        tags.clear();
        if (tagsNode == null) return;
        try {
            List<?> tagList = tagsNode.node("tags").childrenList();
            for (Object obj : tagList) {
                if (obj instanceof ConfigurationNode node) {
                    Tag tag = node.get(Tag.class);
                    if (tag != null && tag.getKey() != null && !tag.getKey().isEmpty()) {
                        tags.put(tag.getKey().toLowerCase(), tag);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load tags", e);
        }
    }

    private void loadPlayerData() {
        playerData.clear();
        if (playerDataNode == null) return;
        try {
            ConfigurationNode playersNode = playerDataNode.node("players");
            for (Object key : playersNode.childrenMap().keySet()) {
                String uuidStr = key.toString();
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationNode playerNode = playersNode.node(uuidStr);
                    PlayerTagData data = playerNode.get(PlayerTagData.class);
                    if (data != null) {
                        data.setUuid(uuid);
                        playerData.put(uuid, data);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid UUID in player data: {}", uuidStr);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load player data", e);
        }
    }

    public Collection<Tag> getAllTags() {
        return tags.values();
    }

    public Tag getTag(String key) {
        return key != null ? tags.get(key.toLowerCase()) : null;
    }

    public void saveTag(Tag tag) {
        if (tag == null || tag.getKey() == null) return;
        tags.put(tag.getKey().toLowerCase(), tag);
        persistTags();
    }

    public void deleteTag(String key) {
        if (key != null) {
            tags.remove(key.toLowerCase());
            persistTags();
        }
    }

    private void persistTags() {
        if (tagsNode == null) return;
        try {
            tagsNode.node("tags").set(null);
            List<Map<String, Object>> tagList = new ArrayList<>();
            for (Tag tag : tags.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("key", tag.getKey());
                map.put("name", tag.getName());
                map.put("icon", tag.getIcon());
                map.put("permission", tag.getPermission());
                map.put("requires-purchase", tag.isRequiresPurchase());
                map.put("price", tag.getPrice());
                map.put("enabled", tag.isEnabled());
                map.put("priority", tag.getPriority());
                tagList.add(map);
            }
            tagsNode.node("tags").set(tagList);
            tagsLoader.save(tagsNode);
        } catch (IOException e) {
            logger.error("Failed to save tags", e);
        }
    }

    public PlayerTagData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public PlayerTagData getOrCreatePlayerData(UUID uuid, String playerName) {
        return playerData.computeIfAbsent(uuid, u -> {
            PlayerTagData data = new PlayerTagData(uuid, playerName);
            persistPlayerData();
            return data;
        });
    }

    public void savePlayerData(PlayerTagData data) {
        if (data == null || data.getUuid() == null) return;
        playerData.put(data.getUuid(), data);
        persistPlayerData();
    }

    private void persistPlayerData() {
        if (playerDataNode == null) return;
        try {
            playerDataNode.node("players").set(null);
            Map<String, Object> playersMap = new LinkedHashMap<>();
            for (PlayerTagData data : playerData.values()) {
                if (data.getUuid() != null) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("player-name", data.getPlayerName());
                    map.put("owned-tags", new ArrayList<>(data.getOwnedTags()));
                    map.put("equipped-tag", data.getEquippedTag());
                    playersMap.put(data.getUuid().toString(), map);
                }
            }
            playerDataNode.node("players").set(playersMap);
            playerDataLoader.save(playerDataNode);
        } catch (IOException e) {
            logger.error("Failed to save player data", e);
        }
    }

    public void reload() {
        try {
            tagsNode = tagsLoader.load();
            playerDataNode = playerDataLoader.load();
            loadTags();
            loadPlayerData();
            logger.info("Reloaded {} tags and {} player data entries", tags.size(), playerData.size());
        } catch (IOException e) {
            logger.error("Failed to reload storage", e);
        }
    }

    public void flush() {
        persistTags();
        persistPlayerData();
        logger.info("Flushed all data to disk");
    }

    private void migrateOldPlayerDataFormat() {
        try {
            ConfigurationNode playersNode = playerDataNode.node("players");
            boolean needsMigration = false;

            for (ConfigurationNode playerNode : playersNode.childrenMap().values()) {
                if (playerNode.node("name").virtual()) continue;

                if (!playerNode.node("player-name").virtual()) continue;

                String oldName = playerNode.node("name").getString();
                String oldEquipped = playerNode.node("equipped").getString("");

                if (oldName != null || !oldEquipped.isEmpty()) {
                    if (oldName != null) {
                        playerNode.node("player-name").set(oldName);
                        playerNode.node("name").set(null);
                    }
                    playerNode.node("equipped-tag").set(oldEquipped);
                    playerNode.node("equipped").set(null);
                    needsMigration = true;
                }
            }

            if (needsMigration) {
                playerDataLoader.save(playerDataNode);
                loadPlayerData();
                logger.info("Migrated player data to new format");
            }
        } catch (Exception e) {
            logger.warn("Failed to migrate old player data format", e);
        }
    }
}