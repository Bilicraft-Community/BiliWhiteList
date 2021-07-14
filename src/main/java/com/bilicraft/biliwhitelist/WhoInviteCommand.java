package com.bilicraft.biliwhitelist;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.UUID;

public class WhoInviteCommand extends Command {
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public WhoInviteCommand(String name) {
        super(name);
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "用法: /whoinvite <玩家ID>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在查询，请稍后...");
        UUID uuid;
        try {
            uuid = MojangAPI.getUUID(args[0]);
        } catch (IOException exception) {
            sender.sendMessages(ChatColor.RED + "网络错误，请稍后重试。错误代码：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (APIException exception) {
            sender.sendMessages(ChatColor.RED + "Mojang API返回了错误响应：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (InvalidPlayerException e) {
            sender.sendMessages(ChatColor.RED + "您所邀请的玩家不存在，请检查用户名输入是否正确");
            return;
        }
        String inviterStr = BiliWhiteList.instance.getInviteRecord(uuid);
        if (inviterStr == null) {
            sender.sendMessages(ChatColor.RED + "该玩家无人邀请");
            return;
        }
        UUID inviter = UUID.fromString(inviterStr);
        String name;
        try {
            name = MojangAPI.getName(inviter);
        } catch (IOException exception) {
            sender.sendMessages(ChatColor.RED + "网络错误，请稍后重试。错误代码：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (APIException exception) {
            sender.sendMessages(ChatColor.RED + "Mojang API返回了错误响应：" + ChatColor.GRAY + exception.getMessage());
            return;
        } catch (InvalidPlayerException e) {
            sender.sendMessages(ChatColor.RED + "邀请人是非有效玩家。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "查询结果: " + ChatColor.YELLOW + name);
    }
}
