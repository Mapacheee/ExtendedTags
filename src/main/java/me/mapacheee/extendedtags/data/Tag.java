package me.mapacheee.extendedtags.data;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Tag {

    private String key;
    private String name;
    private String icon;
    private String permission;
    private boolean requiresPurchase;
    private double price;
    private boolean enabled;
    private int priority;

    public Tag() {
        this.key = "";
        this.name = "<white>Tag";
        this.icon = "NAME_TAG";
        this.permission = "";
        this.requiresPurchase = false;
        this.price = 0.0;
        this.enabled = true;
        this.priority = 0;
    }

    public Tag(String key, String name) {
        this();
        this.key = key.toLowerCase();
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission != null ? permission : "";
    }

    public boolean isRequiresPurchase() {
        return requiresPurchase;
    }

    public void setRequiresPurchase(boolean requiresPurchase) {
        this.requiresPurchase = requiresPurchase;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Math.max(0.0, price);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}