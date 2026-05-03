package me.mapacheee.extendedtags.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.ReloadServiceManager;
import me.mapacheee.extendedtags.config.EtMessages;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.gui.TagEditorGui;
import me.mapacheee.extendedtags.gui.TagsGui;
import me.mapacheee.extendedtags.service.PlayerTagService;
import me.mapacheee.extendedtags.service.TagService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@CommandComponent
public final class TagsCommand {

    private final TagService tagService;
    private final PlayerTagService playerTagService;
    private final TagsGui tagsGui;
    private final TagEditorGui tagEditorGui;
    private final Container<EtMessages> messages;
    private final ReloadServiceManager reloadServiceManager;

    @Inject
    public TagsCommand(TagService tagService, PlayerTagService playerTagService, TagsGui tagsGui,
                       TagEditorGui tagEditorGui, Container<EtMessages> messages,
                       ReloadServiceManager reloadServiceManager) {
        this.tagService = tagService;
        this.playerTagService = playerTagService;
        this.tagsGui = tagsGui;
        this.tagEditorGui = tagEditorGui;
        this.messages = messages;
        this.reloadServiceManager = reloadServiceManager;
    }

    @Command("tags")
    public void tags(Source source) {
        if (!(source.source() instanceof Player player)) return;
        tagsGui.open(player);
    }

    @Command("tag")
    public void tagRoot(Source source) {
        tags(source);
    }

    @Command("extendedtags reload")
    @Permission("extendedtags.admin")
    public void reload(Source source) {
        reloadServiceManager.reload();
        send(source, messages.get().pluginReloaded());
    }

    @Command("tag create <key> <prefix>")
    @Permission("extendedtags.admin")
    public void createTag(Source source, @Argument("key") String key, @Argument("prefix") String prefix) {
        String normalized = key.toLowerCase(Locale.ROOT);
        if (tagService.getTag(normalized) != null) {
            send(source, messages.get().tagAlreadyExists(), Placeholder.parsed("tag", normalized));
            return;
        }
        Tag tag = new Tag(normalized, prefix);
        tagService.saveTag(tag);
        send(source, messages.get().tagCreated(), Placeholder.parsed("tag", normalized));
    }

    @Command("tag delete <key>")
    @Permission("extendedtags.admin")
    public void deleteTag(Source source, @Argument("key") String key) {
        if (!tagService.deleteTag(key)) {
            send(source, messages.get().tagNotFound());
            return;
        }
        send(source, messages.get().tagDeleted(), Placeholder.parsed("tag", key.toLowerCase(Locale.ROOT)));
    }

    @Command("tag setname <key> <name>")
    @Permission("extendedtags.admin")
    public void setName(Source source, @Argument("key") String key, @Argument("name") String name) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setName(name);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag seticon <key> <material>")
    @Permission("extendedtags.admin")
    public void setIcon(Source source, @Argument("key") String key, @Argument("material") String material) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        Material mat = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
        if (mat == null || !mat.isItem()) { send(source, messages.get().invalidMaterial()); return; }
        tag.setIcon(mat.name());
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag setpermission <key> <permission>")
    @Permission("extendedtags.admin")
    public void setPermission(Source source, @Argument("key") String key, @Argument("permission") String permission) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setPermission(permission.equalsIgnoreCase("none") ? "" : permission);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag setprice <key> <price>")
    @Permission("extendedtags.admin")
    public void setPrice(Source source, @Argument("key") String key, @Argument("price") double price) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setPrice(price);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag setrequirespurchase <key> <value>")
    @Permission("extendedtags.admin")
    public void setRequiresPurchase(Source source, @Argument("key") String key, @Argument("value") boolean value) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setRequiresPurchase(value);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag setenabled <key> <value>")
    @Permission("extendedtags.admin")
    public void setEnabled(Source source, @Argument("key") String key, @Argument("value") boolean value) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setEnabled(value);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag setpriority <key> <priority>")
    @Permission("extendedtags.admin")
    public void setPriority(Source source, @Argument("key") String key, @Argument("priority") int priority) {
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        tag.setPriority(priority);
        tagService.saveTag(tag);
        send(source, messages.get().tagUpdated(), Placeholder.parsed("tag", tag.getKey()));
    }

    @Command("tag grant <target> <key>")
    @Permission("extendedtags.admin")
    public void grantTag(Source source, @Argument("target") String target, @Argument("key") String key) {
        OfflinePlayer offline = resolveTarget(target);
        if (offline == null) { send(source, messages.get().targetNotFound()); return; }
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        playerTagService.grantTag(offline.getUniqueId(), offline.getName(), key.toLowerCase(Locale.ROOT));
        send(source, messages.get().tagGranted(), Placeholder.parsed("tag", key.toLowerCase()),
                Placeholder.parsed("player", nameOrUuid(offline)));
    }

    @Command("tag revoke <target> <key>")
    @Permission("extendedtags.admin")
    public void revokeTag(Source source, @Argument("target") String target, @Argument("key") String key) {
        OfflinePlayer offline = resolveTarget(target);
        if (offline == null) { send(source, messages.get().targetNotFound()); return; }
        Tag tag = tagService.getTag(key);
        if (tag == null) { send(source, messages.get().tagNotFound()); return; }
        playerTagService.revokeTag(offline.getUniqueId(), offline.getName(), key.toLowerCase(Locale.ROOT));
        send(source, messages.get().tagRevoked(), Placeholder.parsed("tag", key.toLowerCase()),
                Placeholder.parsed("player", nameOrUuid(offline)));
    }

    @Command("tag set <target> <key>")
    @Permission("extendedtags.admin")
    public void setTag(Source source, @Argument("target") String target, @Argument("key") String key) {
        OfflinePlayer offline = resolveTarget(target);
        if (offline == null) { send(source, messages.get().targetNotFound()); return; }

        if (offline.isOnline()) {
            boolean success;
            if ("none".equalsIgnoreCase(key)) {
                success = playerTagService.setEquippedTag(offline.getPlayer(), null);
            } else {
                success = playerTagService.setEquippedTag(offline.getPlayer(), key);
            }
            if (success) {
                if ("none".equalsIgnoreCase(key)) {
                    send(source, messages.get().tagUnequipped(), Placeholder.parsed("player", nameOrUuid(offline)));
                } else {
                    Tag tag = tagService.getTag(key);
                    String displayTag = tag != null ? tag.getName() : key;
                    String fullMsg = messages.get().prefix() + messages.get().tagEquipped()
                            .replace("<tag>", displayTag)
                            .replace("<player>", nameOrUuid(offline));
                    source.source().sendMessage(MiniMessage.miniMessage().deserialize(fullMsg));
                }
            } else {
                send(source, messages.get().tagNotFound());
            }
        }
    }

    @Command("tag editor")
    @Permission("extendedtags.admin")
    public void openEditorList(Source source) {
        if (!(source.source() instanceof Player player)) return;
        tagEditorGui.openList(player);
    }

    private OfflinePlayer resolveTarget(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) return online;
        if (isUuid(input)) return Bukkit.getOfflinePlayer(UUID.fromString(input));
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(input))
                .findFirst().orElse(null);
    }

    private boolean isUuid(String val) {
        return val.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }

    private void send(Source source, String msg, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... placeholders) {
        source.source().sendMessage(MiniMessage.miniMessage().deserialize(messages.get().prefix() + msg, placeholders));
    }

    private String nameOrUuid(OfflinePlayer p) {
        return p.getName() != null ? p.getName() : p.getUniqueId().toString();
    }
}