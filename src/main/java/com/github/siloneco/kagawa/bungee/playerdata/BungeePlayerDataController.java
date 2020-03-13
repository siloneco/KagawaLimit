package com.github.siloneco.kagawa.bungee.playerdata;

import com.github.siloneco.kagawa.bungee.BungeeKagawaLimit;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BungeePlayerDataController {

    private final BungeeKagawaLimit plugin;
    private final List<UUID> uuidList = new ArrayList<>();
    private HashMap<UUID, Integer> joiningSeconds = new HashMap<>();

    private HashMap<UUID, Long> lastExecuted = new HashMap<>();

    public boolean load() {
        try {
            File file = new File(plugin.getDataFolder(), "players.yml");
            if (!file.exists()) {
                return true;
            }
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            List<String> uuidStrList = configuration.getStringList("EnablePlayers");
            uuidList.addAll(uuidStrList.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()));

            Calendar lastExecutedCalendar = Calendar.getInstance();
            lastExecutedCalendar.setTimeInMillis(configuration.getLong("Saved", 0L));
            if (lastExecutedCalendar.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)
                    && System.currentTimeMillis() - configuration.getLong("Saved", 0L) <= (1000L * 60L * 60L * 24L)) {
                for (String key : configuration.getSection("JoiningSeconds").getKeys()) {
                    int sec = configuration.getInt("JoiningSeconds." + key);
                    joiningSeconds.put(UUID.fromString(key), sec);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addKagawaPlayer(UUID uuid) {
        if (!uuidList.contains(uuid)) {
            uuidList.add(uuid);
            lastExecuted.put(uuid, System.currentTimeMillis());
        }
    }

    public void removeKagawaPlayer(UUID uuid) {
        if (uuidList.contains(uuid)) {
            uuidList.remove(uuid);

            if (lastExecuted.containsKey(uuid)) {
                lastExecuted.remove(uuid, System.currentTimeMillis());
            }
        }
    }

    public boolean isKagawaPlayer(UUID uuid) {
        return uuidList.contains(uuid);
    }

    public int getRemaining(UUID uuid) {
        if (!uuidList.contains(uuid)) {
            return -1;
        }
        return 3600 - joiningSeconds.getOrDefault(uuid, 0);
    }

    public boolean canJoinPlayer(UUID uuid) {
        if (!uuidList.contains(uuid)) {
            return true;
        }
        if (!joiningSeconds.containsKey(uuid)) {
            return true;
        }
        if (joiningSeconds.get(uuid) > 3600) {
            return false;
        }
        return true;
    }

    public boolean save() {
        try {
            File file = new File(plugin.getDataFolder(), "players.yml");

            Configuration configuration = new Configuration();

            List<String> uuidStrList = uuidList.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());

            configuration.set("EnablePlayers", uuidStrList);

            for (UUID uuid : uuidList) {
                int sec = joiningSeconds.getOrDefault(uuid, 0);
                if (sec > 0)
                    configuration.set("JoiningSeconds." + uuid.toString(), sec);
            }

            configuration.set("Saved", System.currentTimeMillis());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void playerJoined(UUID uuid) {
        if (!uuidList.contains(uuid)) {
            return;
        }
        lastExecuted.put(uuid, System.currentTimeMillis());
    }

    public void execute(ProxiedPlayer p) {
        UUID uuid = p.getUniqueId();
        if (!uuidList.contains(uuid)) {
            return;
        }

        Calendar lastExecutedCalendar = Calendar.getInstance();
        lastExecutedCalendar.setTimeInMillis(lastExecuted.getOrDefault(uuid, System.currentTimeMillis()));
        if (lastExecutedCalendar.get(Calendar.DATE) != Calendar.getInstance().get(Calendar.DATE)) {
            joiningSeconds.put(uuid, 0);
            lastExecuted.put(uuid, System.currentTimeMillis());
            return;
        }

        long elapsedMilliSeconds = System.currentTimeMillis() - lastExecuted.getOrDefault(uuid, System.currentTimeMillis());
        int elapsedSeconds = (int) (elapsedMilliSeconds / 1000);

        joiningSeconds.put(uuid, joiningSeconds.getOrDefault(uuid, 0) + elapsedSeconds);

        if (joiningSeconds.get(uuid) > 3600) {
            plugin.getProxy().getPlayer(uuid).disconnect(new TextComponent("あなたは1時間以上サーバーに参加できません！\n")
                    , new TextComponent("香川政府のせいです\n")
                    , new TextComponent("あ～あ"));
            return;
        }

        lastExecuted.put(uuid, System.currentTimeMillis());
    }

    public void executeForAll() {
        for (UUID uuid : uuidList) {
            ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
            if (p != null) {
                execute(p);
            }
        }
    }
}
