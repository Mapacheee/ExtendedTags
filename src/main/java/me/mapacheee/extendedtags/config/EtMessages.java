package me.mapacheee.extendedtags.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
@Configurate("messages")
public record EtMessages(
        @Setting("prefix") String prefix,

        @Setting("plugin-reloaded") String pluginReloaded,
        @Setting("tag-created") String tagCreated,
        @Setting("tag-deleted") String tagDeleted,
        @Setting("tag-updated") String tagUpdated,
        @Setting("tag-not-found") String tagNotFound,
        @Setting("tag-already-exists") String tagAlreadyExists,
        @Setting("tag-equipped") String tagEquipped,
        @Setting("tag-unequipped") String tagUnequipped,
        @Setting("tag-granted") String tagGranted,
        @Setting("tag-purchased") String tagPurchased,
        @Setting("tag-revoked") String tagRevoked,
        @Setting("target-not-found") String targetNotFound,
        @Setting("invalid-material") String invalidMaterial,

        @Setting("tags-gui-title") String tagsGuiTitle,
        @Setting("tags-gui-no-tags") String tagsGuiNoTags,
        @Setting("tags-gui-previous-page") String tagsGuiPreviousPage,
        @Setting("tags-gui-next-page") String tagsGuiNextPage,
        @Setting("tags-gui-unequip-item") String tagsGuiUnequipItem,
        @Setting("tags-gui-price-lore") String tagsGuiPriceLore,
        @Setting("tags-gui-selected-lore") String tagsGuiSelectedLore,
        @Setting("tags-gui-click-to-unequip") String tagsGuiClickToUnequip,
        @Setting("tags-gui-available-lore") String tagsGuiAvailableLore,
        @Setting("tags-gui-click-to-equip") String tagsGuiClickToEquip,
        @Setting("tags-gui-unavailable-purchase-lore") String tagsGuiUnavailablePurchaseLore,
        @Setting("tags-gui-click-to-buy") String tagsGuiClickToBuy,
        @Setting("tags-gui-requires-higher-rank-lore") String tagsGuiRequiresHigherRankLore,
        @Setting("tags-gui-current-rank-lore") String tagsGuiCurrentRankLore,
        @Setting("tags-gui-unavailable-permission-lore") String tagsGuiUnavailablePermissionLore,
        @Setting("tags-gui-permission-required-lore") String tagsGuiPermissionRequiredLore,

        @Setting("requires-higher-rank-tag") String requiresHigherRankTag,
        @Setting("no-permission-tag") String noPermissionTag,
        @Setting("economy-unavailable") String economyUnavailable,
        @Setting("insufficient-funds-tag") String insufficientFundsTag,

        @Setting("tag-editor-title") String tagEditorTitle,
        @Setting("tag-editor-no-tags") String tagEditorNoTags,
        @Setting("tag-editor-previous-page") String tagEditorPreviousPage,
        @Setting("tag-editor-next-page") String tagEditorNextPage,
        @Setting("tag-editor-create-item") String tagEditorCreateItem,
        @Setting("tag-editor-delete-item") String tagEditorDeleteItem,
        @Setting("tag-editor-delete-lore") List<String> tagEditorDeleteLore,
        @Setting("tag-editor-back-item") String tagEditorBackItem,
        @Setting("tag-editor-back-lore") List<String> tagEditorBackLore,
        @Setting("tag-editor-save-item") String tagEditorSaveItem,
        @Setting("tag-editor-save-lore") List<String> tagEditorSaveLore,
        @Setting("tag-editor-key-lore") List<String> tagEditorKeyLore,
        @Setting("tag-editor-name-lore") List<String> tagEditorNameLore,
        @Setting("tag-editor-icon-lore") List<String> tagEditorIconLore,
        @Setting("tag-editor-permission-lore") List<String> tagEditorPermissionLore,
        @Setting("tag-editor-requires-purchase-lore") List<String> tagEditorRequiresPurchaseLore,
        @Setting("tag-editor-price-lore") List<String> tagEditorPriceLore,
        @Setting("tag-editor-enabled-lore") List<String> tagEditorEnabledLore,
        @Setting("tag-editor-priority-lore") List<String> tagEditorPriorityLore
) {
public static EtMessages defaults() {
        return new EtMessages(
                "<dark_gray>[<gold>ExtendedTags<dark_gray>] <gray>",
                "<gray>Plugin reloaded successfully.</gray>",
                "<gray>Tag created: <tag></gray>",
                "<gray>Tag deleted: <tag></gray>",
                "<gray>Tag updated: <tag></gray>",
                "<color:#BD3F32>Tag not found.</color>",
                "<color:#BD3F32>Tag already exists: <tag></color>",
                "<gray>Equipped tag: <tag></gray>",
                "<gray>Tag unequipped.</gray>",
                "<gray>Tag granted to <player>: <tag></gray>",
                "<gray>Tag purchased: <tag> for <price></gray>",
                "<gray>Tag revoked from <player>: <tag></gray>",
                "<color:#BD3F32>Target player not found.</color>",
                "<color:#BD3F32>Invalid material.</color>",

                "<dark_gray>Tags <gray>(<gold><page><gray>/<gold><pages><gray>)",
                "<gray>No tags available.</gray>",
                "<gray>← Previous",
                "<gray>Next →",
                "<color:#BD3F32>Unequip Tag</color>",

                "<yellow>Price: <price>",
                "<green>✓ Selected</green>",
                "<gray>Click to unequip</gray>",
                "<green>Available</green>",
                "<gray>Click to equip</gray>",
                "<color:#BD3F32>Purchase required</color>",
                "<gray>Click to buy</gray>",
                "<color:#BD3F32>Requires higher rank</color>",
                "<gray>Current rank: <rank></gray>",
                "<color:#BD3F32>Permission required</color>",
                "<gray>Permission: <permission></gray>",

                "<color:#BD3F32>Requires higher rank.</color>",
                "<color:#BD3F32>Permission required: <permission></color>",
                "<color:#BD3F32>Economy unavailable.</color>",
                "<color:#BD3F32>Insufficient funds. Price: <price></color>",

                "<dark_gray>Tag Editor <gray>(<gold><page><gray>/<gold><pages><gray>)",
                "<gray>No tags to edit.</gray>",
                "<gray>← Previous</gray>",
                "<gray>Next →</gray>",
                "<gradient:#CB356B:#BD3F32>Create New Tag</gradient>",
"<color:#BD3F32>Delete Tag</color>",
                List.of("<color:#BD3F32>Delete this tag", "<gray>This action cannot be undone"),
                "<dark_gray>← Back</dark_gray>",
                List.of("<gray>Back to list"),
                "<green>Save</green>",
                List.of("<green>Save and exit"),
                List.of("<gray>Tag identifier", "<gray>Click to edit"),
                List.of("<gray>Tag display", "<gray>Click to edit"),
                List.of("<gray>Item icon", "<green>Left click: Use held item", "<yellow>Right click: Enter material"),
                List.of("<gray>Required permission", "<gray>Click to edit"),
                List.of("<gray>Requires purchase", "<gray>Click to toggle"),
                List.of("<gray>Price", "<gray>Click to edit"),
                List.of("<gray>Enabled", "<gray>Click to toggle"),
                List.of("<gray>Priority", "<gray>Click to edit")
        );
    }
}