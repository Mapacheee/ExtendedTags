package me.mapacheee.extendedtags.data;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ConfigSerializable
public class PlayerTagData {

    private UUID uuid;
    private String playerName;
    private Set<String> ownedTags;
    private String equippedTag;

    public PlayerTagData() {
        this.ownedTags = new HashSet<>();
        this.equippedTag = "";
    }

    public PlayerTagData(UUID uuid, String playerName) {
        this();
        this.uuid = uuid;
        this.playerName = playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Set<String> getOwnedTags() {
        return ownedTags;
    }

    public void setOwnedTags(Set<String> ownedTags) {
        this.ownedTags = ownedTags != null ? ownedTags : new HashSet<>();
    }

    public String getEquippedTag() {
        return equippedTag;
    }

    public void setEquippedTag(String equippedTag) {
        this.equippedTag = equippedTag != null ? equippedTag : "";
    }

    public boolean hasTag(String tagKey) {
        return ownedTags != null && ownedTags.contains(tagKey.toLowerCase());
    }

    public void addTag(String tagKey) {
        if (ownedTags == null) {
            ownedTags = new HashSet<>();
        }
        ownedTags.add(tagKey.toLowerCase());
    }

    public void removeTag(String tagKey) {
        if (ownedTags != null) {
            ownedTags.remove(tagKey.toLowerCase());
        }
    }
}