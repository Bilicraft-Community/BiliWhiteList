package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

@SuppressWarnings("deprecation")
public class ServerMarkCommand extends Command {
    private final BiliWhiteList plugin;

    /**
     * Construct a new command.
     *
     * @param name       primary name of this command
     * @param permission the permission node required to execute this command,
     *                   null or empty string allows it to be executed by everyone
     * @param aliases    aliases which map back to this command
     */
    public ServerMarkCommand(BiliWhiteList plugin, String name, String permission, String... aliases) {
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
            sender.sendMessage(ChatColor.RED + "参数错误: /bcservermark <server-name-in-proxy> <bool>");
            return;
        }
        sender.sendMessage(ChatColor.BLUE + "正在处理...");
        if(args[1].equalsIgnoreCase("true")) {
            // Mark require whitelist
            plugin.getWhiteListManager().markServerRequireWhiteList(args[0]);
            sender.sendMessage(ChatColor.GREEN + "设置成功，服务器 "+ args[0] +" 现在需要白名单了");
        }else{
            plugin.getWhiteListManager().unmarkServerRequireWhiteList(args[0]);
            sender.sendMessage(ChatColor.GREEN + "设置成功，服务器 "+ args[0] +" 现在不需要白名单了");
        }

    }
}
