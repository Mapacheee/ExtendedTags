package me.mapacheee.extendedtags.gui;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.listener.ListenerComponent;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.mapacheee.extendedtags.config.EtMessages;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.service.TagService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ListenerComponent
public final class TagEditorGui implements Listener {

    private static final int[] TAG_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final TagService tagService;
    private final Container<EtMessages> messages;
    private final NamespacedKey tagKeyData;
    private final NamespacedKey iconDataKey;
    private final Plugin plugin;
    private final Gson gson;

    private final Map<UUID, EditSession> editingSessions = new ConcurrentHashMap<>();

    @Inject
    public TagEditorGui(Plugin plugin, TagService tagService, Container<EtMessages> messages) {
        this.plugin = plugin;
        this.tagService = tagService;
        this.messages = messages;
        this.tagKeyData = new NamespacedKey(plugin, "tag-editor-key");
        this.iconDataKey = new NamespacedKey(plugin, "tag-editor-icon");
        this.gson = new Gson();
    }

    public void openList(Player player) {
        openList(player, 0);
    }

    public void openList(Player player, int page) {
        List<Tag> tags = new ArrayList<>(tagService.getAllTags());
        int totalPages = Math.max(1, (int) Math.ceil((double) tags.size() / TAG_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        EtMessages msgs = messages.get();

        Inventory inv = Bukkit.createInventory(new EditorInventoryHolder(safePage, false, null), 54,
                MiniMessage.miniMessage().deserialize(msgs.tagEditorTitle(),
                        Placeholder.parsed("page", String.valueOf(safePage + 1)),
                        Placeholder.parsed("pages", String.valueOf(totalPages))));

        int start = safePage * TAG_SLOTS.length;
        int end = Math.min(start + TAG_SLOTS.length, tags.size());

        for (int i = start; i < end; i++) {
            Tag tag = tags.get(i);
            int slot = TAG_SLOTS[i - start];
            inv.setItem(slot, buildTagItem(tag));
        }

        if (tags.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER, msgs.tagEditorNoTags()));
        }

        if (safePage > 0) {
            inv.setItem(45, createItem(Material.ARROW, msgs.tagEditorPreviousPage()));
        }
        if (safePage + 1 < totalPages) {
            inv.setItem(53, createItem(Material.ARROW, msgs.tagEditorNextPage()));
        }
        inv.setItem(49, createItem(Material.EMERALD_BLOCK, msgs.tagEditorCreateItem()));

        player.openInventory(inv);
    }

    public boolean openEditor(Player player, String key) {
        Tag tag = tagService.getTag(key);
        if (tag == null) return false;

        EtMessages msgs = messages.get();

        Inventory inv = Bukkit.createInventory(new EditorInventoryHolder(0, true, key), 36,
                MiniMessage.miniMessage().deserialize("<gradient:#CB356B:#BD3F32>Editing: " + tag.getName()));

        inv.setItem(2, createItemLore(Material.PAPER, "<white>" + tag.getKey(),
                msgs.tagEditorKeyLore().toArray(new String[0])));
        inv.setItem(3, createItemLore(Material.WHITE_BANNER, tag.getName(),
                msgs.tagEditorNameLore().toArray(new String[0])));

        ItemStack iconItem = buildIconItem(tag, msgs.tagEditorIconLore());
        inv.setItem(4, iconItem);

        inv.setItem(5, createItemLore(Material.BOOK, "<white>" + (tag.getPermission().isEmpty() ? "None" : tag.getPermission()),
                msgs.tagEditorPermissionLore().toArray(new String[0])));
        inv.setItem(6, createItemLore(tag.isRequiresPurchase() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE,
                "<white>" + tag.isRequiresPurchase(),
                msgs.tagEditorRequiresPurchaseLore().toArray(new String[0])));

        inv.setItem(11, createItemLore(Material.GOLD_INGOT, "<white>" + tag.getPrice(),
                msgs.tagEditorPriceLore().toArray(new String[0])));
        inv.setItem(12, createItemLore(tag.isEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL,
                "<white>" + tag.isEnabled(),
                msgs.tagEditorEnabledLore().toArray(new String[0])));
        inv.setItem(13, createItemLore(Material.BEACON, "<white>" + tag.getPriority(),
                msgs.tagEditorPriorityLore().toArray(new String[0])));

        inv.setItem(15, createItemLore(Material.BARRIER, msgs.tagEditorDeleteItem(),
                msgs.tagEditorDeleteLore().toArray(new String[0])));
        inv.setItem(17, createItemLore(Material.ARROW, msgs.tagEditorBackItem(),
                msgs.tagEditorBackLore().toArray(new String[0])));

        inv.setItem(26, createItemLore(Material.LIME_STAINED_GLASS, msgs.tagEditorSaveItem(),
                msgs.tagEditorSaveLore().toArray(new String[0])));

        player.openInventory(inv);
        return true;
    }

    private ItemStack buildIconItem(Tag tag, List<String> iconLore) {
        String iconData = tag.getIcon();
        ItemStack item;

        if (iconData == null || iconData.isEmpty()) {
            item = new ItemStack(Material.NAME_TAG);
        } else if (iconData.startsWith("JSON:")) {
            try {
                String json = iconData.substring(5);
                Map<String, Object> map = gson.fromJson(json, Map.class);
                item = ItemStack.deserialize(map);
            } catch (Exception e) {
                item = new ItemStack(Material.NAME_TAG);
            }
        } else {
            Material mat = Material.matchMaterial(iconData);
            item = new ItemStack(mat != null ? mat : Material.NAME_TAG);
        }

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(iconDataKey, PersistentDataType.STRING, "icon");

        String iconType = item.getType().name();
        if (iconData != null && iconData.startsWith("JSON:")) {
            iconType = iconData.substring(5, Math.min(50, iconData.length())) + "...";
        }

        meta.displayName(MiniMessage.miniMessage().deserialize("<white>" + iconType));
        meta.getPersistentDataContainer().set(iconDataKey, PersistentDataType.STRING, "icon");
        meta.lore(iconLore.stream()
                .map(l -> MiniMessage.miniMessage().deserialize(l))
                .toList());
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof EditorInventoryHolder holder)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (!holder.isEditor()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            if (slot == 45) { openList(player, holder.page() - 1); return; }
            if (slot == 53) { openList(player, holder.page() + 1); return; }
            if (slot == 49) { createNewTag(player); return; }

            String key = clicked.getItemMeta().getPersistentDataContainer().get(tagKeyData, PersistentDataType.STRING);
            if (key != null && !key.isEmpty()) {
                openEditor(player, key);
            }
        } else {
            Tag tag = tagService.getTag(holder.tagKey());
            if (tag == null) { openList(player); return; }

            if (slot == 17) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openList(player);
                return;
            }
            if (slot == 15) {
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 0.5f, 1.0f);
                tagService.deleteTag(holder.tagKey());
                send(player, messages.get().tagDeleted(), Placeholder.parsed("tag", holder.tagKey()));
                openList(player);
                return;
            }
            if (slot == 26) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                tagService.saveTag(tag);
                send(player, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
                openList(player);
                return;
            }

            if (slot == 2) {
                startEditing(player, holder.tagKey(), Field.KEY);
            } else if (slot == 3) {
                startEditing(player, holder.tagKey(), Field.NAME);
            } else if (slot == 4) {
                if (event.isLeftClick()) {
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem == null || handItem.getType() == Material.AIR) {
                        send(player, "<red>You must hold an item to set as icon.");
                        return;
                    }

                    Map<String, Object> serialized = handItem.serialize();
                    String json = gson.toJson(serialized);

                    tag.setIcon("JSON:" + json);
                    tagService.saveTag(tag);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    send(player, "<green>Icon updated! (<gold>" + handItem.getType().name() + "<reset>)");
                    openEditor(player, holder.tagKey());
                } else {
                    startEditing(player, holder.tagKey(), Field.ICON_CHAT);
                }
            } else if (slot == 5) {
                startEditing(player, holder.tagKey(), Field.PERMISSION);
            } else if (slot == 6) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                tag.setRequiresPurchase(!tag.isRequiresPurchase());
                tagService.saveTag(tag);
                openEditor(player, holder.tagKey());
            } else if (slot == 11) {
                startEditing(player, holder.tagKey(), Field.PRICE);
            } else if (slot == 12) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                tag.setEnabled(!tag.isEnabled());
                tagService.saveTag(tag);
                openEditor(player, holder.tagKey());
            } else if (slot == 13) {
                startEditing(player, holder.tagKey(), Field.PRIORITY);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        EditSession session = editingSessions.get(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message());

        player.getScheduler().run(plugin, task -> handleEditInput(player, session, input), null);
    }

    private void handleEditInput(Player player, EditSession session, String input) {
        Tag tag = tagService.getTag(session.tagKey());
        if (tag == null) {
            editingSessions.remove(player.getUniqueId());
            return;
        }

        switch (session.field) {
            case KEY:
                if (!input.isEmpty() && !input.equalsIgnoreCase(tag.getKey())) {
                    tagService.deleteTag(tag.getKey());
                    tag.setKey(input);
                    tagService.saveTag(tag);
                }
                break;
            case NAME:
                tag.setName(input);
                tagService.saveTag(tag);
                break;
            case ICON_CHAT:
                Material mat = Material.matchMaterial(input.toUpperCase());
                if (mat != null && mat.isItem()) {
                    tag.setIcon(mat.name());
                    tagService.saveTag(tag);
                } else {
                    send(player, "<red>Invalid material: " + input);
                    editingSessions.remove(player.getUniqueId());
                    return;
                }
                break;
            case PERMISSION:
                tag.setPermission(input.isEmpty() ? "" : input);
                tagService.saveTag(tag);
                break;
            case PRICE:
                try {
                    double price = Math.max(0, Double.parseDouble(input));
                    tag.setPrice(price);
                    tagService.saveTag(tag);
                } catch (NumberFormatException e) {
                    send(player, "<red>Invalid number: " + input);
                    editingSessions.remove(player.getUniqueId());
                    return;
                }
                break;
            case PRIORITY:
                try {
                    int priority = Integer.parseInt(input);
                    tag.setPriority(priority);
                    tagService.saveTag(tag);
                } catch (NumberFormatException e) {
                    send(player, "<red>Invalid number: " + input);
                    editingSessions.remove(player.getUniqueId());
                    return;
                }
                break;
            default:
                break;
        }

        editingSessions.remove(player.getUniqueId());
        player.getScheduler().run(plugin, task -> openEditor(player, tag.getKey()), null);
    }

    private void startEditing(Player player, String tagKey, Field field) {
        editingSessions.put(player.getUniqueId(), new EditSession(tagKey, field));
        player.closeInventory();

        String fieldName = switch (field) {
            case KEY -> "Key";
            case NAME -> "Name";
            case ICON_CHAT -> "Icon (material)";
            case PERMISSION -> "Permission";
            case PRICE -> "Price";
            case PRIORITY -> "Priority";
        };

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        Component titleText = MiniMessage.miniMessage().deserialize(
                "<gradient:#CB356B:#BD3F32>Editing: <white>" + fieldName
        );
        Component subtitleText = MiniMessage.miniMessage().deserialize("<gray>Type the new value in chat");
        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3500),
                Duration.ofMillis(1000)
        );
        player.showTitle(Title.title(titleText, subtitleText, times));

        send(player, "<yellow>Enter new " + fieldName + ":");
    }

    private void createNewTag(Player player) {
        Tag tag = new Tag("new-tag", "<yellow>[New Tag]");
        tagService.saveTag(tag);
        openEditor(player, tag.getKey());
    }

    private ItemStack buildTagItem(Tag tag) {
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
        lore.add("<gray>Key: " + tag.getKey());
        lore.add("<gray>Priority: " + tag.getPriority());
        lore.add("<gray>Enabled: " + tag.isEnabled());
        if (tag.isRequiresPurchase()) {
            lore.add("<yellow>Price: " + tag.getPrice());
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

    private ItemStack createItemLore(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        if (loreLines.length > 0) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(MiniMessage.miniMessage().deserialize(line));
            }
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void send(Player player, String msg) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + msg));
    }

    private void send(Player player, String msg, TagResolver... placeholders) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + msg, placeholders));
    }

    private record EditorInventoryHolder(int page, boolean isEditor, String tagKey) implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    private record EditSession(String tagKey, Field field) {}

    private enum Field {
        KEY, NAME, ICON_CHAT, PERMISSION, PRICE, PRIORITY
    }
}

