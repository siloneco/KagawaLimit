package com.github.siloneco.kagawa.bungee;

import com.github.siloneco.kagawa.bungee.command.KagawaCommand;
import com.github.siloneco.kagawa.bungee.listener.BungeeJoinQuitListener;
import com.github.siloneco.kagawa.bungee.playerdata.BungeePlayerDataController;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

@Getter
public class BungeeKagawaLimit extends Plugin {

    private BungeePlayerDataController controller = null;

    @Override
    public void onEnable() {
        controller = new BungeePlayerDataController(this);
        controller.load();

        getProxy().getPluginManager().registerListener(this, new BungeeJoinQuitListener(this));

        getProxy().getPluginManager().registerCommand(this, new KagawaCommand(this, "kagawa"));

        getProxy().getScheduler().schedule(this, () -> {
            controller.executeForAll();
        }, 1, 1, TimeUnit.SECONDS);

        getLogger().info("Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        controller.save();

        getLogger().info("Plugin Disabled.");
    }
}