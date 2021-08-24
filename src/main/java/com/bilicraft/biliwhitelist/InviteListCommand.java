package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.enginehub.squirrelid.Profile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class InviteListCommand extends Command {
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public InviteListCommand(String name) {
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
            sender.sendMessage(ChatColor.RED + "用法: /invitelist <玩家ID>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在查询，请稍后...");

        try {
            Profile profile = Util.getResolver().findByName(args[0]);
            if (profile == null) {
                sender.sendMessages(ChatColor.RED + "您所邀请的玩家不存在，请检查用户名输入是否正确");
                return;
            }

            List<UUID> inviteds = BiliWhiteList.instance.getInvitedRecords(profile.getUniqueId());
            for (Profile invited : Util.getResolver().findAllByUuid(inviteds)) {
                sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + invited.getName());
            }
        } catch (IOException | InterruptedException e) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + e.getMessage());
        }
    }
}
