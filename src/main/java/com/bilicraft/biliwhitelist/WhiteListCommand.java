package com.bilicraft.biliwhitelist;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.UUID;

public class WhiteListCommand extends Command {
    /**
     * Construct a new command.
     *
     * @param name       primary name of this command
     * @param permission the permission node required to execute this command,
     *                   null or empty string allows it to be executed by everyone
     * @param aliases    aliases which map back to this command
     */
    public WhiteListCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
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
        UUID uuid;
        try {
            uuid = MojangAPI.getUUID(args[1]);
        } catch (IOException exception) {
            sender.sendMessages(ChatColor.RED + "网络错误，请稍后重试。错误代码：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (APIException exception) {
            sender.sendMessages(ChatColor.RED + "Mojang API返回了错误响应：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (InvalidPlayerException e) {
            sender.sendMessages(ChatColor.RED + "该玩家不存在");
            return;
        }


        switch (args[0]) {
            case "add":
                BiliWhiteList.instance.getWhitelisted().add(uuid);
                BiliWhiteList.instance.saveWhitelisted();
                sender.sendMessages(ChatColor.GREEN + "添加成功：" + args[1] + " # " + uuid);
                BiliWhiteList.instance.getLogger().info(ChatColor.GREEN + "白名单添加成功：" + args[1] + " # " + uuid+", 操作员："+sender.getName());
                break;
            case "del":
            case "remove":
                if (BiliWhiteList.instance.getWhitelisted().remove(uuid)) {
                    sender.sendMessages(ChatColor.GREEN + "白名单删除成功：" + args[1] + " # " + uuid);
                    BiliWhiteList.instance.getLogger().info(ChatColor.GREEN + "白名单删除成功：" + args[1] + " # " + uuid+", 操作员："+sender.getName());
                } else {
                    sender.sendMessages(ChatColor.RED + "玩家不在白名单中：" + args[1] + " # " + uuid);
                }
                break;
            case "list":
                sender.sendMessage(ChatColor.GREEN + "白名单玩家：" + BiliWhiteList.instance.getWhitelisted());
                break;
            case "query":
                sender.sendMessage(ChatColor.GREEN+"该玩家白名单状态："+BiliWhiteList.instance.getWhitelisted().contains(uuid));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "参数有误");
        }
    }
}
