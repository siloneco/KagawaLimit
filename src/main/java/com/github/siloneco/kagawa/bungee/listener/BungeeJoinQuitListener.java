package com.github.siloneco.kagawa.bungee.listener;

import com.github.siloneco.kagawa.bungee.BungeeKagawaLimit;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

@RequiredArgsConstructor
public class BungeeJoinQuitListener implements Listener {

    private final BungeeKagawaLimit plugin;

    @EventHandler
    public void onJoin(LoginEvent e) {
        UUID uuid = e.getConnection().getUniqueId();

        if (!plugin.getController().canJoinPlayer(uuid)) {
            e.setCancelled(true);
            e.setCancelReason(new TextComponent("あなたは1時間以上サーバーに参加できません！\n")
                    , new TextComponent("香川県議会のせいです\n")
                    , new TextComponent("あ～あ"));
            return;
        }

        plugin.getController().playerJoined(uuid);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        plugin.getController().execute(e.getPlayer());
    }
}
