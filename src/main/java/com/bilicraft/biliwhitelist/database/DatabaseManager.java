/*
 * This file is a part of project QuickShop, the name is DatabaseManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.bilicraft.biliwhitelist.database;

import com.bilicraft.biliwhitelist.BiliWhiteList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Queued database manager. Use queue to solve run SQL make server lagg issue.
 */
public class DatabaseManager {
    private final Queue<DatabaseTask> sqlQueue = new LinkedBlockingQueue<>();
    @NotNull
    @Getter
    private final AbstractDatabaseCore database;
    private boolean useQueue;
    private final BiliWhiteList plugin;

    /**
     * Queued database manager. Use queue to solve run SQL make server lagg issue.
     *
     * @param plugin plugin main class
     * @param dbCore database core
     * @throws ConnectionException when database connection failed
     */
    public DatabaseManager(@NotNull BiliWhiteList plugin, @NotNull AbstractDatabaseCore dbCore) throws ConnectionException {
        this.plugin = plugin;
        this.database = dbCore;
        init();

    }

    private void init() throws ConnectionException {
        DatabaseConnection connection = database.getConnection();
        try {
            if (!connection.isValid()) {
                throw new ConnectionException("The database does not appear to be valid!");
            }
        } finally {
            connection.release();
        }
    }

    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    public boolean hasTable(@NotNull String table) throws SQLException {
        DatabaseConnection connection = database.getConnection();
        boolean match = false;
        try (ResultSet rs = connection.get().getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    match = true;
                    break;
                }
            }
        } finally {
            connection.release();
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

        DatabaseConnection connection = database.getConnection();
        String query = "SELECT * FROM " + table + " LIMIT 1";
        boolean match = false;
        try (PreparedStatement ps = connection.get().prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnLabel(i).equals(column)) {
                    match = true;
                    break;
                }
            }
        } catch (SQLException e) {
            return match;
        } finally {
            connection.release();
        }
        return match; // Uh, wtf.
    }

    /**
     * Internal method, runTasks in queue.
     */
    private synchronized void runTask() { // synchronized for QUICKSHOP-WX
        if (sqlQueue.isEmpty()) {
            return;
        }
        DatabaseConnection dbconnection = this.database.getConnection();
        //We do not close the connection since is reusable
        Connection connection = dbconnection.get();
        try {
            //start our commit
            connection.setAutoCommit(false);
            while (true) {
                if (!dbconnection.isValid()) {
                    return; // Waiting next crycle and hope it success reconnected.
                }

                DatabaseTask task = sqlQueue.poll();
                if (task == null) {
                    break;
                }

                task.run(connection);
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
            }

        } catch (SQLException sqle) {
            this.plugin
                    .getLogger()
                    .log(Level.WARNING, "Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).", sqle);
        } finally {
            dbconnection.release();
        }
    }

    public DatabaseConnection getConnection(){
        return database.getConnection();
    }

    /**
     * Add DatabaseTask to queue waiting flush to database,
     *
     * @param task The DatabaseTask you want add in queue.
     */
    public void runInstantTask(DatabaseTask task) {
        DatabaseConnection connection = database.getConnection();
        task.run(connection.get());
        connection.release();
    }



    /**
     * Add DatabaseTask to queue waiting flush to database,
     *
     * @param task The DatabaseTask you want add in queue.
     */
    public void addDelayTask(DatabaseTask task) {
        if (useQueue) {
            sqlQueue.offer(task);
        } else {
            runInstantTask(task);
        }
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public synchronized void unInit() {
        plugin.getLogger().info("Please wait for the data to flush its data...");
        runTask();
        database.close();
    }

    /**
     * Represents a connection error, generally when the server can't connect to MySQL or something.
     */
    public static final class ConnectionException extends Exception {
        private static final long serialVersionUID = 8348749992936357317L;

        private ConnectionException(String msg) {
            super(msg);
        }

    }
}