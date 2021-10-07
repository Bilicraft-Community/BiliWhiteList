package com.bilicraft.biliwhitelist.manager;

import com.bilicraft.biliwhitelist.BiliWhiteList;
import lombok.SneakyThrows;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class SilentBanManager {
    private final BiliWhiteList plugin;
    private final File silentBanListFile;
    private Configuration silentBanConfig;

    @SneakyThrows
    public SilentBanManager(BiliWhiteList plugin) {
        this.plugin = plugin;
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.silentBanListFile = new File(plugin.getDataFolder(), "silentban.yml");
        if (!silentBanListFile.exists()) silentBanListFile.createNewFile();
        this.silentBanConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(silentBanListFile);
    }

    public boolean isSilentBanned(UUID player) {
        return silentBanConfig.getStringList("silentbanlist").contains(player.toString());
    }

    @SneakyThrows
    public void silentBan(UUID player) {
        List<String> whitelist = silentBanConfig.getStringList("silentbanlist");
        if (whitelist.contains(player.toString())) return;
        whitelist.add(player.toString());
        silentBanConfig.set("silentbanlist", whitelist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(silentBanConfig, silentBanListFile);
    }

    @SneakyThrows
    public void unSilentBan(UUID player) {
        List<String> whitelist = silentBanConfig.getStringList("silentbanlist");
        whitelist.remove(player.toString());
        silentBanConfig.set("silentbanlist", whitelist);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(silentBanConfig, silentBanListFile);
    }
}
