package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.enginehub.squirrelid.Profile;

import java.io.IOException;
import java.util.UUID;

public class WhiteListCommand extends Command {
    private final BiliWhiteList plugin;

    /**
     * Construct a new command.
     *
     * @param name       primary name of this command
     * @param permission the permission node required to execute this command,
     *                   null or empty string allows it to be executed by everyone
     * @param aliases    aliases which map back to this command
     */
    public WhiteListCommand(BiliWhiteList plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(ChatColor.RED + "您无权执行此操作");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "参数错误: /whitelist <add/remove/list/query> [name/uuid]; 由于偷懒，请使用/whitelist list的时候后面也跟一个有效玩家游戏用户名");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在处理...");

        try {
            Profile profile = plugin.getResolver().findByName(args[1]);
            if (profile == null) {
                sender.sendMessages(ChatColor.RED + "该玩家不存在");
                return;
            }
            UUID uuid = profile.getUniqueId();
            switch (args[0]) {
                case "add":
                    if (plugin.getWhiteListManager().isBlocked(uuid)) {
                        sender.sendMessages(ChatColor.RED + "添加失败：" + args[1] + " 位于回绝名单中");
                        return;
                    }
                    plugin.getWhiteListManager().addWhiteList(uuid);
                    sender.sendMessages(ChatColor.GREEN + "添加成功：" + args[1] + " # " + uuid);
                    plugin.getLogger().info(ChatColor.GREEN + "白名单添加成功：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                    Util.broadcastToAdmins(ChatColor.GREEN + "[广播]白名单添加：" + args[1] + ", 操作员：" + sender.getName());
                    break;
                case "del":
                case "remove":
                    if (plugin.getWhiteListManager().isWhiteListed(uuid)) {
                        plugin.getWhiteListManager().removeWhiteList(uuid);
                        sender.sendMessages(ChatColor.GREEN + "[广播]白名单删除：" + args[1] + " # " + uuid);
                        plugin.getLogger().info(ChatColor.GREEN + "白名单删除成功：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                        Util.broadcastToAdmins(ChatColor.GREEN + "白名单删除成功：" + args[1] + ", 操作员：" + sender.getName());
                    } else {
                        sender.sendMessages(ChatColor.RED + "玩家不在白名单中：" + args[1] + " # " + uuid);
                    }
                    break;
                case "list":
                    sender.sendMessage(ChatColor.GREEN + "白名单玩家：" + plugin.getWhiteListManager().formatAllInWhiteList());
                    break;
                case "query":
                    if (plugin.getWhiteListManager().isBlocked(uuid)) {
                        sender.sendMessages(ChatColor.GREEN + "该玩家白名单状态：回绝");
                        return;
                    }
                    if (!plugin.getWhiteListManager().isWhiteListed(uuid)) {
                        sender.sendMessage(ChatColor.GREEN + "该玩家白名单状态：无白名单");
                        break;
                    }
                    if (plugin.getHistoryManager().getInviter(uuid).isPresent()) {
                        sender.sendMessage(ChatColor.GREEN + "该玩家白名单状态：有白名单 (邀请进入)");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "该玩家白名单状态：有白名单 (管理添加)");
                    }
                    break;
                case "block":
                    if (plugin.getWhiteListManager().isBlocked(uuid)) {
                        sender.sendMessages(ChatColor.RED + "添加失败：" + args[1] + " 已位于回绝名单中");
                        return;
                    }
                    plugin.getWhiteListManager().addBlockList(uuid);
                    if (plugin.getWhiteListManager().isWhiteListed(uuid)) {
                        plugin.getWhiteListManager().removeWhiteList(uuid);
                        sender.sendMessages(ChatColor.YELLOW + "撤销：" + args[1] + " 发现现存白名单，已撤销");
                    }
                    sender.sendMessages(ChatColor.GREEN + "添加成功：" + args[1] + " 现已被回绝");
                    plugin.getLogger().info(ChatColor.GREEN + "回绝：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                    break;
                case "unblock":
                    if (!plugin.getWhiteListManager().isBlocked(uuid)) {
                        sender.sendMessages(ChatColor.RED + "移除失败：" + args[1] + " 没有位于回绝名单中");
                        return;
                    }
                    plugin.getWhiteListManager().removeBlockList(uuid);
                    sender.sendMessages(ChatColor.GREEN + "移除成功：" + args[1] + " 的回绝操作已被撤销");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "参数有误");
            }
        } catch (InterruptedException | IOException e) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + e.getMessage());
        }
    }
}
