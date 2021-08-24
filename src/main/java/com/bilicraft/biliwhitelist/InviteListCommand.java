package com.bilicraft.biliwhitelist;

import com.google.common.collect.ImmutableList;
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
            sender.sendMessage(ChatColor.RED + "用法: /invitelist <玩家ID>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在查询，请稍后...");

        try {
            Profile profile = plugin.getResolver().findByName(args[0]);
            if (profile == null) {
                sender.sendMessages(ChatColor.RED + "您所查询的玩家不存在，请检查用户名输入是否正确");
                return;
            }

            List<UUID> inviteds = plugin.getHistoryManager().getInvited(profile.getUniqueId());
            ImmutableList<Profile> invitesProfile = plugin.getResolver().findAllByUuid(inviteds);
            for (Profile invited : invitesProfile) {
                sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + invited.getName() +"("+invited.getUniqueId()+")");
            }
        } catch (IOException | InterruptedException e) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + e.getMessage());
        }
    }
}
