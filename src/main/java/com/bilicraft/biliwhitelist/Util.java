package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.ProxyServer;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.cache.HashMapCache;
import org.enginehub.squirrelid.cache.ProfileCache;
import org.enginehub.squirrelid.cache.SQLiteCache;
import org.enginehub.squirrelid.resolver.CacheForwardingService;
import org.enginehub.squirrelid.resolver.HttpRepositoryService;
import org.enginehub.squirrelid.resolver.ProfileService;

import java.io.File;
import java.io.IOException;

public class Util {
    private static CacheForwardingService resolver;
    private static SQLiteCache cache;

    static {
        try {
            cache = new SQLiteCache(new File(BiliWhiteList.instance.getDataFolder(),"cache.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
       resolver = new CacheForwardingService(
                HttpRepositoryService.forMinecraft(),
                cache);
    }

    public static void broadcastToAdmins(String content){
        //BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
        ProxyServer.getInstance().broadcast(content);
    }
    public static ProfileService getResolver(){
        return resolver;
    }

    public static SQLiteCache getCache() {
        return cache;
    }

    //    public static void broadcastToAdmins(BaseComponent content){
//        BiliWhiteList.instance.getProxy().getPlayers().stream().filter(p->p.hasPermission("biliwhitelist.admin")).forEach(p->p.sendMessage(content));
//    }

}
