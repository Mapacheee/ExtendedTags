package me.mapacheee.extendedtags.listener;

import com.google.inject.Inject;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.extendedtags.service.PlayerTagService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@ListenerComponent
public final class PlayerTagListener implements Listener {

    private final PlayerTagService playerTagService;

    @Inject
    public PlayerTagListener(PlayerTagService playerTagService) {
        this.playerTagService = playerTagService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerTagService.preload(event.getPlayer());
    }
}