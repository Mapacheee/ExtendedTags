package me.mapacheee.extendedtags.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
@Configurate("config")
public record EtConfig(
        @Setting("gui-title") String guiTitle,
        @Setting("default-tag") String defaultTag
) {
    public static EtConfig defaults() {
        return new EtConfig(
                "<dark_gray>Tags <gray>(<gold><page><gray>/<gold><pages><gray>)",
                ""
        );
    }
}