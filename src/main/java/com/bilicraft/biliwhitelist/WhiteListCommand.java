package com.bilicraft.biliwhitelist;

import com.bilicraft.biliwhitelist.manager.WhiteListManager;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.enginehub.squirrelid.Profile;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
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
            sender.sendMessage(ChatColor.RED + "参数错误: /bcwhitelist <add/remove/list/query/silentban/unsilentban> [name/uuid]; 由于偷懒，请使用/whitelist list的时候后面也跟一个有效玩家游戏用户名");
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
                    switch (plugin.getWhiteListManager().checkWhiteList(uuid)){
                        case BLOCKED:
                            sender.sendMessages(ChatColor.RED + "添加失败：" + args[1] + " 位于回绝名单中");
                            return;
                        case WHITELISTED:
                            sender.sendMessages(ChatColor.YELLOW + "添加失败：" + args[1] + " 已在白名单中");
                            return;
                        case NO_RECORD:
                            plugin.getWhiteListManager().addWhite(uuid,new UUID(0,0));
                            sender.sendMessages(ChatColor.GREEN + "添加成功：" + args[1] + " # " + uuid);
                            plugin.getLogger().info(ChatColor.GREEN + "白名单添加成功：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                            return;
                    }
                    break;
                case "del":
                case "remove":
                    switch (plugin.getWhiteListManager().checkWhiteList(uuid)){
                        case NO_RECORD:
                            sender.sendMessages(ChatColor.RED + "删除失败：" + args[1] + " 不在白名单或者回绝列表中");
                            return;
                        case WHITELISTED:
                            plugin.getWhiteListManager().removeWhite(uuid);
                            plugin.getLogger().info(ChatColor.GREEN + "白名单删除成功：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                            sender.sendMessages(ChatColor.GREEN + "白名单删除：" + args[1] + " # " + uuid);
                            return;
                        case BLOCKED:
                            plugin.getWhiteListManager().removeWhite(uuid);
                            plugin.getLogger().info(ChatColor.YELLOW+ "回绝删除成功，如有需要，请重新添加白名单：" + args[1] + " # " + uuid + ", 操作员：" + sender.getName());
                            sender.sendMessages(ChatColor.YELLOW + "回绝删除：" + args[1] + " # " + uuid);
                            return;
                    }
                    break;
                case "list":
                    StringJoiner builder = new StringJoiner(",","","");
                    sender.sendMessage(ChatColor.BLUE + "请稍等，这可能需要一会儿...");
                    List<UUID> queryResultList = plugin.getWhiteListManager().queryRecords().stream().map(
                            WhiteListManager.QueryResult::getUuid
                    ).collect(Collectors.toList());
                    ImmutableList<Profile> queryList =   plugin.getResolver().findAllByUuid(queryResultList);
                    for (Profile pro : queryList) {
                        builder.add(pro.getName());
                    }
                    sender.sendMessage(ChatColor.GREEN + "白名单玩家：" + builder);
                    break;
                case "query":
                    switch (plugin.getWhiteListManager().checkWhiteList(uuid)){
                        case BLOCKED:
                            sender.sendMessage(ChatColor.RED+"目标玩家处于回绝名单中，无法进入内服，且无法再添加他的白名单");
                            return;
                        case WHITELISTED:
                            sender.sendMessage(ChatColor.GREEN+"目标玩家处于白名单中，可进入内服");
                            return;
                        case NO_RECORD:
                            sender.sendMessage(ChatColor.YELLOW+"目标玩家不在任何名单中，只能进入外服");
                            return;
                    }
                    break;
                case "block":
                    switch (plugin.getWhiteListManager().checkWhiteList(uuid)){
                        case BLOCKED:
                            sender.sendMessage(ChatColor.RED+"目标玩家已处于回绝名单中");
                            return;
                        case NO_RECORD:
                        case WHITELISTED:
                            plugin.getWhiteListManager().setBlock(uuid,true);
                            sender.sendMessage(ChatColor.GREEN+"成功设置目标玩家状态为回绝");
                            return;
                    }
                    break;
                case "unblock":
                    sender.sendMessages(ChatColor.LIGHT_PURPLE + "该命令不再可用，请使用/bcwhitelist remove代替");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "参数有误");
            }
        } catch (InterruptedException | IOException e) {
            sender.sendMessages(ChatColor.RED + "内部错误，请稍后重试。错误代码：" + ChatColor.GRAY + e.getMessage());
        }
    }
}
