package org.ch4rlesexe.cEconomy.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLStorage {

    private Connection connection;

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLStorage(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username,
                    password
            );
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS stocks (" +
                    "stock_name VARCHAR(50) PRIMARY KEY," +
                    "value DOUBLE NOT NULL" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_balances (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "balance DOUBLE NOT NULL" +
                    ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
