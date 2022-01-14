/*
 * This file is a part of project QuickShop, the name is SimpleDatabaseHelper.java
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
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A Util to execute all SQLs.
 */
public class SimpleDatabaseHelper {


    @NotNull
    private final DatabaseManager manager;

    @NotNull
    private final BiliWhiteList plugin;

    public SimpleDatabaseHelper(@NotNull BiliWhiteList plugin, @NotNull DatabaseManager manager) throws SQLException {
        this.plugin = plugin;
        this.manager = manager;
    }
    public boolean createColumn(@NotNull String tableName, @NotNull String columnName, @NotNull DataType type) {

        try {
            String table = tableName;
            if (manager.hasColumn(table, columnName)) {
                return false;
            }
            String sqlString;
            if (manager.getDatabase() instanceof MySQLCore) {
                sqlString = "alter table " + table + " add " + columnName + " " + type.getDatatype().getMysql();
            } else {
                sqlString = "alter table " + table + " add column " + columnName + " " + type.getDatatype().getSqlite();
            }
            if (type.getLength() != null) {
                sqlString += "(" + type.getLength().toString() + ") ";
            }
            manager.runInstantTask(new DatabaseTask(sqlString, new DatabaseTask.Task() {
                @Override
                public void edit(PreparedStatement ps) {
                }

                @Override
                public void onFailed(SQLException e) {
                  e.printStackTrace();
                }
            }));
            return true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return false;
        }
    }

    public SimpleWarpedResultSet selectTable(String table) throws SQLException {
        DatabaseConnection databaseConnection = manager.getDatabase().getConnection();
        Statement st = databaseConnection.get().createStatement();
        String sql = "SELECT * FROM " + table;
        ResultSet resultSet = st.executeQuery(sql);
        //Resource closes will complete in this class
        return new SimpleWarpedResultSet(st, resultSet, databaseConnection);
    }
}
