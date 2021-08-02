package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.chat.BaseComponent;

public class Util {
    public static void broadcastToAdmins(String content){
        BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
    }
    public static void broadcastToAdmins(BaseComponent content){
        BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
    }
}
