package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.enginehub.squirrelid.Profile;

import java.io.IOException;

public class WhoInviteCommand extends Command {
    private final BiliWhiteList plugin;
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public WhoInviteCommand(BiliWhiteList plugin,String name) {
        super(name);
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
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "用法: /whoinvite <玩家ID>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在查询，请稍后...");

        try {
            Profile profile = plugin.getResolver().findByName(args[0]);
            if(profile == null){
                sender.sendMessages(ChatColor.RED + "您所邀请的玩家不存在，请检查用户名输入是否正确");
                return;
            }
            if (!plugin.getHistoryManager().getInviter(profile.getUniqueId()).isPresent()) {
                sender.sendMessages(ChatColor.RED + "该玩家无人邀请");
                return;
            }
            Profile inviter = plugin.getResolver().findByUuid(plugin.getHistoryManager().getInviter(profile.getUniqueId()).get());
            if(inviter == null){
                sender.sendMessages(ChatColor.RED + "该玩家无人邀请或网络故障");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + "查询结果: " + ChatColor.YELLOW + inviter.getName());
        } catch (IOException | InterruptedException exception) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + exception.getMessage());
        }

    }
}
