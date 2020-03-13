package com.github.siloneco.kagawa.bungee.command;

import com.github.siloneco.kagawa.bungee.BungeeKagawaLimit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class KagawaCommand extends Command {

    private final BungeeKagawaLimit plugin;

    public KagawaCommand(BungeeKagawaLimit plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "このコマンドはプレイヤーのみ実行可能です"));
            return;
        }
        if (args.length <= 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "/" + getName() + " [on/off/time]"));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;

        if (args[0].equalsIgnoreCase("on")) {
            if (plugin.getController().isKagawaPlayer(p.getUniqueId())) {
                p.sendMessage(new TextComponent(ChatColor.RED + "あなたはすでに香川民と登録されています！"));
                return;
            }
            plugin.getController().addKagawaPlayer(p.getUniqueId());
            p.sendMessage(new TextComponent(ChatColor.GREEN + "香川県民として登録しました！ 1時間しか遊べません！"));
            return;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (!plugin.getController().isKagawaPlayer(p.getUniqueId())) {
                p.sendMessage(new TextComponent(ChatColor.RED + "あなたは香川民と登録されていません！"));
                return;
            }
            plugin.getController().removeKagawaPlayer(p.getUniqueId());
            p.sendMessage(new TextComponent(ChatColor.GREEN + "他県へ引っ越しました！ 1時間以上遊べます！"));
            return;
        }

        if (args[0].equalsIgnoreCase("time")) {
            if (!plugin.getController().isKagawaPlayer(p.getUniqueId())) {
                p.sendMessage(new TextComponent(ChatColor.RED + "あなたは香川民と登録されていません！"));
                return;
            }

            int remaining = plugin.getController().getRemaining(p.getUniqueId());
            p.sendMessage(new TextComponent(ChatColor.GREEN + "あと" + ChatColor.YELLOW + "" + remaining + "秒" + ChatColor.GREEN + "遊べます！"));
            return;
        }

        p.sendMessage(new TextComponent(ChatColor.RED + "/" + getName() + " [on/off/time]"));
    }
}
