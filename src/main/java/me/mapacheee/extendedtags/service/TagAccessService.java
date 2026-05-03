package me.mapacheee.extendedtags.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedtags.data.PlayerTagData;
import me.mapacheee.extendedtags.data.Tag;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

@Service
public final class TagAccessService {

    private final Logger logger;

    @Inject
    public TagAccessService(Logger logger) {
        this.logger = logger;
    }

    public enum Availability {
        AVAILABLE,
        UNAVAILABLE_PURCHASE,
        UNAVAILABLE_RANK,
        UNAVAILABLE_PERMISSION,
        UNAVAILABLE_DISABLED
    }

    public Availability check(Player player, Tag tag, PlayerTagData playerData) {
        if (!tag.isEnabled()) {
            return Availability.UNAVAILABLE_DISABLED;
        }

        if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
            return Availability.UNAVAILABLE_PERMISSION;
        }

        if (playerData.hasTag(tag.getKey())) {
            return Availability.AVAILABLE;
        }

        if (tag.isRequiresPurchase()) {
            return Availability.UNAVAILABLE_PURCHASE;
        }

        return Availability.AVAILABLE;
    }

    public boolean canEquip(Player player, Tag tag, PlayerTagData playerData) {
        return check(player, tag, playerData) == Availability.AVAILABLE;
    }

    public boolean canPurchase(Player player, Tag tag) {
        return tag.isRequiresPurchase() && tag.isEnabled();
    }
}