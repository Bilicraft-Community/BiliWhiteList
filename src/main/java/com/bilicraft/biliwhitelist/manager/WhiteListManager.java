package com.bilicraft.biliwhitelist.manager;

import com.bilicraft.biliwhitelist.BiliWhiteList;
import com.bilicraft.biliwhitelist.Util;
import com.bilicraft.biliwhitelist.database.DatabaseConnection;
import com.bilicraft.biliwhitelist.database.DatabaseTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WhiteListManager {
    private final BiliWhiteList plugin;
    private final String recordsTableName = "biliwhitelist_records";
    private final String serversTableName = "biliwhitelist_servers";

    @SneakyThrows
    public WhiteListManager(BiliWhiteList plugin) {
        this.plugin = plugin;
    }
    private void checkDatabase() throws SQLException {
        if(!plugin.getDatabaseManager().hasTable(recordsTableName)){
            String sql = "CREATE TABLE `"+recordsTableName+"`  (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(36) NOT NULL,\n" +
                    "  `blocked` tinyint(1) NOT NULL,\n" +
                    "  `inviter` varchar(36) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`, `uuid`),\n" +
                    "  UNIQUE INDEX `index`(`id`, `uuid`) USING HASH,\n" +
                    "  INDEX `whitelist-query`(`id`, `uuid`, `blocked`) USING BTREE\n" +
                    ");";
            plugin.getDatabaseManager().runInstantTask(new DatabaseTask(sql));
        }
        if(!plugin.getDatabaseManager().hasTable(serversTableName)){
            String sql = "CREATE TABLE `"+serversTableName+"`  (\n" +
                    "  `id` int NOT NULL,\n" +
                    "  `server_name` varchar(255) NOT NULL,\n" +
                    "  `require_whitelist` tinyint(1) NOT NULL,\n" +
                    "        PRIMARY KEY (`id`, `server_name`),\n" +
                    "        INDEX `index`(`id`, `server_name`, `require_whitelist`) USING BTREE\n" +
                    ");";
            plugin.getDatabaseManager().runInstantTask(new DatabaseTask(sql));
        }
    }
    @Nullable
    public QueryResult queryRecord(@NotNull UUID player){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT FROM " + recordsTableName+" WHERE uuid=?");
            preparedStatement.setString(1,player.toString());
            ResultSet set = preparedStatement.executeQuery();
            if(!set.next())
                return null;
            return new QueryResult(
                    UUID.fromString(set.getString("uuid")),
                    Util.boolFromInt(set.getInt("blocked")),
                    UUID.fromString(set.getString("inviter"))
            );
        }catch (SQLException exception){
            exception.printStackTrace();
            return null;
        }
    }

    @NotNull
    public List<QueryResult> queryRecords(){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + recordsTableName+" WHERE 1=1 LIMIT 1");
            ResultSet set = preparedStatement.executeQuery();
            List<QueryResult> results = new ArrayList<>();
            while(set.next()) {
                results.add(new QueryResult(
                        UUID.fromString(set.getString("uuid")),
                        Util.boolFromInt(set.getInt("blocked")),
                        UUID.fromString(set.getString("inviter"))
                        )
                );
            }
            return results;
        }catch (SQLException exception){
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    private boolean addRecord(@NotNull UUID player,@NotNull QueryResult result){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `"+ recordsTableName+"` (`id`, `uuid`, `blocked`, `inviter`) VALUES (0, ?, ?, ?)");
            preparedStatement.setString(1,player.toString());
            preparedStatement.setInt(2,Util.boolToInt(result.isBlocked()));
            preparedStatement.setString(3,result.getInviter().toString());
           return preparedStatement.execute();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }

    private boolean removeRecord(@NotNull UUID player){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `"+ recordsTableName+"` WHERE `uuid` = ?");
            preparedStatement.setString(1,player.toString());
            return preparedStatement.execute();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }

    private boolean updateRecord(@NotNull UUID player, @NotNull QueryResult result){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `"+ recordsTableName+"` SET `blocked` = ?, `inviter` = ? WHERE  `uuid` = ?");
            preparedStatement.setInt(1, Util.boolToInt(result.isBlocked()));
            preparedStatement.setString(2,player.toString());
            preparedStatement.setString(3,result.getInviter().toString());

            return preparedStatement.execute();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }

    public boolean isSeverRequireWhiteList(@NotNull String server){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `"+ recordsTableName+"` WHERE server = ?");
            preparedStatement.setString(1,server);
            return preparedStatement.executeQuery().next();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }

    public boolean markServerRequireWhiteList(@NotNull String server){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO  `"+ recordsTableName+"` (`id`,`server`) VALUES (?,?)");
            preparedStatement.setInt(1,0);
            preparedStatement.setString(2,server);
            return preparedStatement.executeQuery().next();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }
    public boolean unmarkServerRequireWhiteList(@NotNull String server){
        try(DatabaseConnection databaseConnection = plugin.getDatabaseManager().getConnection()) {
            Connection connection = databaseConnection.get();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `"+ recordsTableName+"` WHERE `server` = ?");
            preparedStatement.setString(1,server);
            return preparedStatement.execute();
        }catch (SQLException exception){
            exception.printStackTrace();
            return false;
        }
    }
    public void addWhite(@NotNull UUID player, @NotNull UUID inviter) {
        if(checkWhiteList(player) != RecordStatus.NO_RECORD)
            return;
        addRecord(player,new QueryResult(player,false,inviter));
    }
    public void removeWhite(@NotNull UUID player) {
        if(checkWhiteList(player) == RecordStatus.NO_RECORD)
            return;
        removeRecord(player);
    }
    public void setBlock(@NotNull UUID player, boolean blocked){
        QueryResult queryResult = queryRecord(player);
        if(!blocked && queryResult == null)
            return;
        if(queryResult != null){
            queryResult.setBlocked(blocked);
            updateRecord(player,queryResult);
        }else {
            addRecord(player, new QueryResult(player, true, new UUID(0, 0)));
        }
    }

    @NotNull
    public RecordStatus checkWhiteList(@NotNull UUID player) {
        QueryResult result = queryRecord(player);
        if(result == null)
            return RecordStatus.NO_RECORD;
        if(result.isBlocked())
            return RecordStatus.BLOCKED;
        return RecordStatus.WHITELISTED;
    }

    public enum RecordStatus{
       NO_RECORD, BLOCKED, WHITELISTED
    }
    @AllArgsConstructor
    @Data
    public static class QueryResult{
        @NotNull
        private UUID uuid;
        private boolean blocked;
        @NotNull
        private UUID inviter;
    }
}
