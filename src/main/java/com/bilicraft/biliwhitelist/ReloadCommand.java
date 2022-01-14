package com.bilicraft.biliwhitelist;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {
    private final BiliWhiteList plugin;
    /**
     * Construct a new command with no permissions or aliases.
     *
     * @param name the name of this command
     */
    public ReloadCommand(BiliWhiteList plugin, String name) {
        super(name,"whitelist.admin");
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
        plugin.reload();
        sender.sendMessage("Whitelist data has been reloaded.");
    }
}
