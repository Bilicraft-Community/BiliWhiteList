package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ProxyServer;

public class Util {

    public static void broadcast(String content){
        //BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
        ProxyServer.getInstance().broadcast(content);
    }

    public static boolean boolFromInt(int i){
        return i != 0;
    }

    public static int boolToInt(boolean bool){
        if(bool)
            return 1;
        return 0;
    }


    //    public static void broadcastToAdmins(BaseComponent content){
//        BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
//    }

}
