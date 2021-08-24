package com.bilicraft.biliwhitelist;

import com.bilicraft.biliwhitelist.manager.HistoryManager;
import com.bilicraft.biliwhitelist.manager.WhiteListManager;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.cache.SQLiteCache;
import org.enginehub.squirrelid.resolver.CacheForwardingService;
import org.enginehub.squirrelid.resolver.HttpRepositoryService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BiliWhiteList extends Plugin implements Listener {
    public static BiliWhiteList instance;
    @Getter
    private CacheForwardingService resolver;
    private SQLiteCache cache;
    private Configuration config;
    @Getter
    private final HistoryManager historyManager = new HistoryManager(this);
    @Getter
    private final WhiteListManager whiteListManager = new WhiteListManager(this);

    @Override
    public void onLoad() {
        instance = this;
    }

    @SneakyThrows
    public Configuration getConfig() {
        if(config == null){
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        }
        return config;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();
        // 初始化NameMapping
        this.cache = new SQLiteCache(new File(getDataFolder(),"cache.db"));
        //this.cache = new HashMapCache();
        this.resolver = new CacheForwardingService(HttpRepositoryService.forMinecraft(), cache);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhiteListCommand(this,"whitelist", "whitelist.admin"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new InviteCommand(this,"invite"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhoInviteCommand(this,"whoinvite"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new InviteListCommand(this,"invitelist"));
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        getProxy().getPlayers().forEach(player-> cache.put(new Profile(player.getUniqueId(),player.getName())));
    }

    private Map<String, String> getForcedHosts() {
        Map<String, String> hosts = new HashMap<>();
        for (ListenerInfo info : this.getProxy().getConfigurationAdapter().getListeners()) {
            for (Map.Entry<String, String> host : info.getForcedHosts().entrySet()) {
                hosts.put(host.getKey().toLowerCase(), host.getValue().toLowerCase());
            }
        }
        return hosts;
    }

    private String getForcedHost(String virtualHost) { //Maybe null
        return getForcedHosts().get(virtualHost);
    }



    @Override
    public void onDisable() {

    }

    private void saveDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverSwitch(ServerConnectEvent event) {
        if (getConfig().getStringList("excludes").contains(event.getTarget().getName())) {
            getLogger().info("玩家 " + event.getPlayer().getName() + " # " + event.getPlayer().getUniqueId() + " 例外列表放行： " + event.getTarget().getName());
            return;
        }
        UUID playerUniqueId = event.getPlayer().getUniqueId();
        if (!whiteListManager.isWhiteListed(playerUniqueId)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(TextComponent.fromLegacyText("您不在 Bilicraft 白名单中，无法连接到 " + event.getTarget().getName() + " 服务器。请申请白名单或联系其他玩家邀请。"));
        } else {
            getLogger().info("玩家 " + event.getPlayer().getName() + " # " + event.getPlayer().getUniqueId() + " 白名单放行： " + event.getTarget().getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(LoginEvent event) {
        UUID playerUniqueId = event.getConnection().getUniqueId();
        if (playerUniqueId == null) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("Bilicraft 是正版服务器，请使用正版 Minecraft 账号登录"));
            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 不是正版 Minecraft 账号，已拒绝");
            return;
        }
        this.cache.put(new Profile(playerUniqueId,event.getConnection().getName()));
        String forcedHost = getForcedHost(event.getConnection().getVirtualHost().getHostString());
        if (getConfig().getStringList("excludes").contains(forcedHost)) {
            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 例外列表放行： " + forcedHost);
            return;
        }
        if (!whiteListManager.isWhiteListed(playerUniqueId)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("您不在 Bilicraft 白名单中，请申请白名单或联系其他玩家邀请"));
            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 没有白名单，已拒绝");
        } else {
            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 白名单放行");
        }
    }
}
