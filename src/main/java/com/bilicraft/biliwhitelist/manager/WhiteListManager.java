package com.bilicraft.biliwhitelist.manager;

import com.bilicraft.biliwhitelist.BiliWhiteList;
import lombok.SneakyThrows;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class WhiteListManager {
    private final BiliWhiteList plugin;
    private final File whiteListFile;
    private Configuration whiteListConfig;

    @SneakyThrows
    public WhiteListManager(BiliWhiteList plugin) {
        this.plugin = plugin;
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.whiteListFile = new File(plugin.getDataFolder(), "whitelist.yml");
        if (!whiteListFile.exists()) whiteListFile.createNewFile();
        this.whiteListConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(whiteListFile);
    }

    public boolean isWhiteListed(UUID player) {
        return whiteListConfig.getStringList("whitelist").contains(player.toString());
    }

    public boolean isBlocked(UUID player) {
        return whiteListConfig.getStringList("blocklist").contains(player.toString());
    }

    public boolean isAllowed(UUID player) {
        return isWhiteListed(player) && !isBlocked(player);
    }

    @SneakyThrows
    public void addWhiteList(UUID player) {
        List<String> whitelist = whiteListConfig.getStringList("whitelist");
        if (whitelist.contains(player.toString())) return;
        whitelist.add(player.toString());
        whiteListConfig.set("whitelist", whitelist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(whiteListConfig, whiteListFile);
    }

    @SneakyThrows
    public void removeWhiteList(UUID player) {
        List<String> whitelist = whiteListConfig.getStringList("whitelist");
        whitelist.remove(player.toString());
        whiteListConfig.set("whitelist", whitelist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(whiteListConfig, whiteListFile);
    }

    @SneakyThrows
    public void addBlockList(UUID player) {
        List<String> blocklist = whiteListConfig.getStringList("blocklist");
        if (blocklist.contains(player.toString())) return;
        blocklist.add(player.toString());
        whiteListConfig.set("blocklist", blocklist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(whiteListConfig, whiteListFile);
    }

    @SneakyThrows
    public void removeBlockList(UUID player) {
        List<String> blocklist = whiteListConfig.getStringList("blocklist");
        blocklist.remove(player.toString());
        whiteListConfig.set("blocklist", blocklist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(whiteListConfig, whiteListFile);
    }

    public String formatAllInWhiteList() {
        StringBuilder builder = new StringBuilder();
        whiteListConfig.getStringList("whitelist").forEach(player -> builder.append(player).append(", "));
        return builder.toString();
    }

    public String formatAllInBlockList() {
        StringBuilder builder = new StringBuilder();
        whiteListConfig.getStringList("blocklist").forEach(player -> builder.append(player).append(", "));
        return builder.toString();
    }
}
