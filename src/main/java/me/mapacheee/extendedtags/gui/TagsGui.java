package me.mapacheee.extendedtags.gui;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.extendedtags.config.EtMessages;
import me.mapacheee.extendedtags.data.PlayerTagData;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.service.EconomyService;
import me.mapacheee.extendedtags.service.PlayerTagService;
import me.mapacheee.extendedtags.service.TagAccessService;
import me.mapacheee.extendedtags.service.TagService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ListenerComponent
public final class TagsGui implements Listener {

    private static final int[] TAG_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final TagService tagService;
    private final PlayerTagService playerTagService;
    private final TagAccessService tagAccessService;
    private final EconomyService economyService;
    private final Container<EtMessages> messages;
    private final NamespacedKey tagKeyData;
    private final Gson gson = new Gson();

    @Inject
    public TagsGui(Plugin plugin, TagService tagService, PlayerTagService playerTagService,
                   TagAccessService tagAccessService, EconomyService economyService,
                   Container<EtMessages> messages) {
        this.tagService = tagService;
        this.playerTagService = playerTagService;
        this.tagAccessService = tagAccessService;
        this.economyService = economyService;
        this.messages = messages;
        this.tagKeyData = new NamespacedKey(plugin, "tag-key");
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        List<Tag> tags = new ArrayList<>(tagService.getAllTags());
        int totalPages = Math.max(1, (int) Math.ceil((double) tags.size() / TAG_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(new TagInventoryHolder(safePage), 54,
                MiniMessage.miniMessage().deserialize(messages.get().tagsGuiTitle(),
                        Placeholder.parsed("page", String.valueOf(safePage + 1)),
                        Placeholder.parsed("pages", String.valueOf(totalPages))));

        PlayerTagData playerData = playerTagService.getOrCreate(player);

        int start = safePage * TAG_SLOTS.length;
        int end = Math.min(start + TAG_SLOTS.length, tags.size());

        for (int i = start; i < end; i++) {
            Tag tag = tags.get(i);
            int slot = TAG_SLOTS[i - start];
            inv.setItem(slot, buildTagItem(player, playerData, tag));
        }

        if (tags.isEmpty()) {
            ItemStack empty = createItem(Material.BARRIER, messages.get().tagsGuiNoTags());
            inv.setItem(22, empty);
        }

        if (safePage > 0) {
            inv.setItem(45, createItem(Material.ARROW, messages.get().tagsGuiPreviousPage()));
        }
        if (safePage + 1 < totalPages) {
            inv.setItem(53, createItem(Material.ARROW, messages.get().tagsGuiNextPage()));
        }
        inv.setItem(49, createItem(Material.BARRIER, messages.get().tagsGuiUnequipItem()));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof TagInventoryHolder holder)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

        if (slot == 45) { open(player, holder.page - 1); return; }
        if (slot == 53) { open(player, holder.page + 1); return; }
        if (slot == 49) {
            playerTagService.setEquippedTag(player, null);
            send(player, messages.get().tagUnequipped());
            open(player, holder.page);
            return;
        }

        String tagKey = clicked.getItemMeta().getPersistentDataContainer().get(tagKeyData, PersistentDataType.STRING);
        if (tagKey == null || tagKey.isEmpty()) return;

        Tag tag = tagService.getTag(tagKey);
        if (tag == null) return;

        PlayerTagData data = playerTagService.getOrCreate(player);

        if (tagKey.equalsIgnoreCase(data.getEquippedTag())) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 0.5f, 1.0f);
            playerTagService.setEquippedTag(player, null);
            send(player, messages.get().tagUnequipped());
            open(player, holder.page);
            return;
        }

        TagAccessService.Availability availability = tagAccessService.check(player, tag, data);

        if (availability == TagAccessService.Availability.UNAVAILABLE_PERMISSION) {
            if (tag.isRequiresPurchase()) {
                send(player, messages.get().noPermissionTag(), Placeholder.parsed("permission", tag.getPermission()));
            } else {
                send(player, messages.get().noPermissionTag(), Placeholder.parsed("permission", tag.getPermission()));
            }
            return;
        }

        if (availability == TagAccessService.Availability.UNAVAILABLE_DISABLED) {
            send(player, messages.get().tagNotFound());
            return;
        }

        if (availability == TagAccessService.Availability.UNAVAILABLE_PURCHASE) {
            handlePurchase(player, data, tag, holder.page);
            return;
        }

        if (playerTagService.setEquippedTag(player, tag.getKey())) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + messages.get().tagEquipped()
                    .replace("<tag>", tag.getName())));
            open(player, holder.page);
        }
    }

    private void handlePurchase(Player player, PlayerTagData data, Tag tag, int page) {
        if (!economyService.isAvailable()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            send(player, messages.get().economyUnavailable());
            return;
        }

        double price = tag.getPrice();
        if (!economyService.has(player, price)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            send(player, messages.get().insufficientFundsTag(), Placeholder.parsed("price", economyService.format(price)));
            return;
        }

        if (!economyService.withdraw(player, price)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            send(player, messages.get().insufficientFundsTag(), Placeholder.parsed("price", economyService.format(price)));
            return;
        }

        playerTagService.grantTag(player.getUniqueId(), player.getName(), tag.getKey());
        playerTagService.setEquippedTag(player, tag.getKey());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + messages.get().tagPurchased()
                .replace("<tag>", tag.getName())
                .replace("<price>", economyService.format(tag.getPrice()))));
        open(player, page);
    }

    private ItemStack buildTagItem(Player player, PlayerTagData data, Tag tag) {
        String iconData = tag.getIcon();
        ItemStack item;

        if (iconData != null && iconData.startsWith("JSON:")) {
            try {
                String json = iconData.substring(5);
                Map<String, Object> map = gson.fromJson(json, Map.class);
                item = ItemStack.deserialize(map);
            } catch (Exception e) {
                item = new ItemStack(Material.NAME_TAG);
            }
        } else {
            Material mat = Material.matchMaterial(iconData);
            if (mat == null) mat = Material.NAME_TAG;
            item = new ItemStack(mat);
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(tag.getName()));

        List<String> lore = new ArrayList<>();

        boolean selected = tag.getKey().equalsIgnoreCase(data.getEquippedTag());
        TagAccessService.Availability availability = tagAccessService.check(player, tag, data);

        if (selected) {
            lore.add(messages.get().tagsGuiSelectedLore());
            lore.add(messages.get().tagsGuiClickToUnequip());
        } else if (availability == TagAccessService.Availability.AVAILABLE) {
            lore.add(messages.get().tagsGuiAvailableLore());
            lore.add(messages.get().tagsGuiClickToEquip());
        } else if (availability == TagAccessService.Availability.UNAVAILABLE_PURCHASE) {
            lore.add(messages.get().tagsGuiPriceLore().replace("<price>", economyService.format(tag.getPrice())));
            lore.add(messages.get().tagsGuiUnavailablePurchaseLore());
            lore.add(messages.get().tagsGuiClickToBuy());
        } else if (availability == TagAccessService.Availability.UNAVAILABLE_PERMISSION) {
            lore.add(messages.get().tagsGuiUnavailablePermissionLore());
            lore.add(messages.get().tagsGuiPermissionRequiredLore().replace("<permission>", tag.getPermission()));
        }

        meta.lore(lore.stream().map(l -> MiniMessage.miniMessage().deserialize(l)).toList());
        meta.getPersistentDataContainer().set(tagKeyData, PersistentDataType.STRING, tag.getKey());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        item.setItemMeta(meta);
        return item;
    }

    private void send(Player player, String msg) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + msg));
    }

    private void send(Player player, String msg, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... placeholders) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + msg, placeholders));
    }

    private record TagInventoryHolder(int page) implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
}