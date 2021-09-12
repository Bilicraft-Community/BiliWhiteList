package com.bilicraft.biliwhitelist.manager;

import com.bilicraft.biliwhitelist.BiliWhiteList;
import lombok.SneakyThrows;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HistoryManager {
    private final BiliWhiteList plugin;
    private final File historyFile;
    private Configuration historyConfig;
    @SneakyThrows
    public HistoryManager(BiliWhiteList plugin){
        this.plugin = plugin;
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.historyFile = new File(plugin.getDataFolder(), "history.yml");
        if(!historyFile.exists()) historyFile.createNewFile();
        this.historyConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(historyFile);
    }

    @SneakyThrows
    public void record(UUID inviter, UUID invited){
        historyConfig.set(invited.toString(), inviter.toString());
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(historyConfig, historyFile);
    }

    @SneakyThrows
    public void delete(UUID invited){
        historyConfig.set(invited.toString(), null);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(historyConfig, historyFile);
    }

    public List<UUID> getInvited(UUID inviter){
        List<UUID> list = new ArrayList<>();
        historyConfig.getKeys().forEach(key->{
            UUID valueUniqueId = UUID.fromString(historyConfig.getString(key));
            UUID keyUniqueId = UUID.fromString(key);
            if(valueUniqueId.equals(inviter)){
                list.add(keyUniqueId);
            }
        });
        return list;
    }

    public Optional<UUID> getInviter(UUID invited){
        String inviter = historyConfig.getString(invited.toString());
        if(inviter == null || inviter.isEmpty())
            return Optional.empty();
        return Optional.of(UUID.fromString(inviter));
    }
}
