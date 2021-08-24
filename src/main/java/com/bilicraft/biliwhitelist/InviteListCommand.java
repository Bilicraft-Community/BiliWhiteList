package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.enginehub.squirrelid.Profile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class InviteListCommand extends Command {
    private final BiliWhiteList plugin;
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public InviteListCommand(BiliWhiteList plugin, String name) {
        super(name,"whitelist.staff");
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
            sender.sendMessage(ChatColor.RED + "用法: /invitelist <玩家ID>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在查询，请稍后（这可能需要很长一段时间）...");

        try {
            Profile profile = plugin.getResolver().findByName(args[0]);
            if (profile == null) {
                sender.sendMessages(ChatColor.RED + "您所查询的玩家不存在，请检查用户名输入是否正确");
                return;
            }

            List<UUID> inviteds = plugin.getHistoryManager().getInvited(profile.getUniqueId());

            for (UUID invited : inviteds) {
                try {
                    Profile invitedProfile = plugin.getResolver().findByUuid(invited);
                    if (invitedProfile != null) {
                        sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + invitedProfile.getName() +ChatColor.DARK_GRAY+ " (" + invitedProfile.getUniqueId() + ")");
                    }
                }catch (IllegalArgumentException exception){
                    sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + "读取失败" +ChatColor.DARK_GRAY+ " (" + invited.toString() + ")");
                }
            }
            sender.sendMessage(ChatColor.BLUE + "查询完毕！");
        } catch (IOException | InterruptedException e) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + e.getMessage());
        }
    }
}
