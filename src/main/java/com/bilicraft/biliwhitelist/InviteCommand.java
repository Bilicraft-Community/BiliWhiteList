package com.bilicraft.biliwhitelist;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.UUID;

public class InviteCommand extends Command {
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public InviteCommand(String name) {
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
        if(!(sender instanceof ProxiedPlayer)){
            sender.sendMessage(ChatColor.RED + "该命令仅控制台可执行");
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "命令输入有误，正确输入：/invite <游戏ID>");
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(ChatColor.AQUA + "您正在邀请玩家 " + ChatColor.YELLOW + ChatColor.AQUA + args[0] + " 加入 Bilicraft");
            sender.sendMessage(ChatColor.AQUA + "邀请成功后，您邀请的玩家将会自动获得 Bilicraft 白名单");
            sender.sendMessage(ChatColor.YELLOW + "注意：如果您邀请的玩家发生了违规行为，您将会承担连带责任");
            sender.sendMessage(ChatColor.GREEN + "确认邀请请输入 " + ChatColor.GOLD + "/invite " + args[0] + " confirm");
            return;
        }

        if (args.length == 2) {
            sender.sendMessage(ChatColor.BLUE + "正在处理，请稍等...");
            UUID uuid;
            try {
                uuid = MojangAPI.getUUID(args[0]);
            } catch (IOException exception) {
                sender.sendMessages(ChatColor.RED+"网络错误，请稍后重试。错误代码："+ChatColor.GRAY+exception.getMessage());
                return;
            } catch (APIException exception) {
                sender.sendMessages(ChatColor.RED+"Mojang API返回了错误响应："+ChatColor.GRAY+exception.getMessage());
                return;
            }catch (InvalidPlayerException e){
                sender.sendMessages(ChatColor.RED+"您所邀请的玩家不存在，请检查用户名输入是否正确");
                return;
            }

            if(BiliWhiteList.instance.getWhitelisted().contains(uuid)) {
                sender.sendMessages(ChatColor.RED + "您所邀请的玩家当前已在白名单中，无需重复邀请");
                return;
            }
            BiliWhiteList.instance.inviteRecord(((ProxiedPlayer) sender).getUniqueId(),uuid);
            BiliWhiteList.instance.getWhitelisted().add(uuid);
            BiliWhiteList.instance.saveWhitelisted();
            sender.sendMessage(ChatColor.GREEN + "邀请成功");
            BiliWhiteList.instance.getLogger().info("玩家 " + sender.getName() + " 邀请了 " + args[0]);
        }
    }
}
