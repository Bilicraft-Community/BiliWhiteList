package com.bilicraft.biliwhitelist;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BiliWhiteList extends Plugin implements Listener {
    private final Set<UUID> whitelisted = new HashSet<>();
    public static BiliWhiteList instance;
    private Configuration excludes;
    private Configuration configuration;

    @Override
    public void onLoad() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        excludes = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        getLogger().info("Loaded excluded servers: " + excludes.getStringList("excludes"));
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file1 = new File(getDataFolder(), "history.yml");
        File file2 = new File(getDataFolder(), "whitelist.yml");
        if (!file1.exists())
            file1.createNewFile();
        if (!file2.exists())
            file2.createNewFile();

        loadWhitelisted();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhiteListCommand("whitelist", "whitelist.admin"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new InviteCommand("invite"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhoInviteCommand("whoinvite"));
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }


    private void loadWhitelisted() {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "whitelist.yml"));
            whitelisted.clear();
            configuration.getStringList("whitelist").forEach(ustr -> whitelisted.add(UUID.fromString(ustr)));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void saveWhitelisted() {
        try {
            configuration.set("whitelist", whitelisted.stream().map(UUID::toString).collect(Collectors.toList()));
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "whitelist.yml"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void inviteRecord(UUID inviter, UUID invited) {
        try {
            Configuration history = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "history.yml"));
            history.set(invited.toString(), inviter.toString());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(history, new File(getDataFolder(), "history.yml"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Get inviter
    public String getInviteRecord(UUID invited) {
        try {
            Configuration history = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "history.yml"));
            return history.getString(invited.toString());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
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

    public Set<UUID> getWhitelisted() {
        return whitelisted;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverSwitch(ServerConnectEvent event) {
        if (excludes.getStringList("excludes").contains(event.getTarget().getName())) {
            getLogger().info("玩家 " + event.getPlayer().getName() + " # " + event.getPlayer().getUniqueId() + " 例外列表放行： " + event.getTarget().getName());
            return;
        }
        UUID playerUniqueId = event.getPlayer().getUniqueId();
        if (!this.whitelisted.contains(playerUniqueId)) {
            event.setCancelled(true);
            if (event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
                event.getPlayer().disconnect(TextComponent.fromLegacyText("您不在 Bilicraft 白名单中，请申请白名单或联系其他玩家邀请"));
                getLogger().info("玩家 " + event.getPlayer().getName() + " # " + event.getPlayer().getUniqueId() + " 没有白名单，已拒绝: " + event.getTarget().getName());
            } else {
                event.getPlayer().sendMessage(TextComponent.fromLegacyText("您不在 Bilicraft 白名单中，无法连接到 " + event.getTarget().getName() + " 服务器。请申请白名单或联系其他玩家邀请。"));
            }
        } else {
            getLogger().info("玩家 " + event.getPlayer().getName() + " # " + event.getPlayer().getUniqueId() + " 白名单放行： " + event.getTarget().getName());
        }
    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onJoin(LoginEvent event) {
//        UUID playerUniqueId = event.getConnection().getUniqueId();
//        if (playerUniqueId == null) {
//            event.setCancelled(true);
//            event.setCancelReason(TextComponent.fromLegacyText("Bilicraft 是正版服务器，请使用正版 Minecraft 账号登录"));
//            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 不是正版 Minecraft 账号，已拒绝");
//            return;
//        }
//        if (!this.whitelisted.contains(playerUniqueId)) {
//            event.setCancelled(true);
//            event.setCancelReason(TextComponent.fromLegacyText("您不在 Bilicraft 白名单中，请申请白名单或联系其他玩家邀请"));
//            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 没有白名单，已拒绝");
//        } else {
//            getLogger().info("玩家 " + event.getConnection().getName() + " # " + event.getConnection().getUniqueId() + " 白名单放行");
//        }
//
//    }
}
