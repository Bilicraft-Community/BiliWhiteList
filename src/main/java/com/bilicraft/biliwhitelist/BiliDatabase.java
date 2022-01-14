package com.bilicraft.biliwhitelist;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class BiliDatabase {
    private final HikariDataSource ds ;
    public BiliDatabase( @NotNull BiliWhiteList plugin,
                         @NotNull String host,
                         @NotNull String user,
                         @NotNull String pass,
                         @NotNull String database,
                         int port,
                         boolean useSSL){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl( "jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(pass);
        config.addDataSourceProperty("connection-timeout","60000");
        config.addDataSourceProperty("validation-timeout","3000");
        config.addDataSourceProperty("idle-timeout","60000");
        config.addDataSourceProperty("login-timeout","5");
        config.addDataSourceProperty("maxLifeTime","60000");
        config.addDataSourceProperty("maximum-pool-size","8");
        config.addDataSourceProperty("minimum-idle","10");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.ds = new HikariDataSource(config);
    }

    @SneakyThrows
    public Connection getConnection(){
        return ds.getConnection();
    }

    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    public boolean hasTable(@NotNull String table) throws SQLException {
        boolean match = false;
        try (ResultSet rs = ds.getConnection().getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    /**
     * Returns true if the given table has the given column
     *
     * @param table  The table
     * @param column The column
     * @return True if the given table has the given column
     * @throws SQLException If the database isn't connected
     */
    public boolean hasColumn(@NotNull String table, @NotNull String column) throws SQLException {
        if (!hasTable(table)) {
            return false;
        }

        Connection connection = ds.getConnection();
        String query = "SELECT * FROM " + table + " LIMIT 1";
        boolean match = false;
        try (PreparedStatement ps = ds.getConnection().prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnLabel(i).equals(column)) {
                    match = true;
                    break;
                }
            }
        } catch (SQLException e) {
            return match;
        }
        return match; // Uh, wtf.
    }

}
