package org.ch4rlesexe.cEconomy.services;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ch4rlesexe.cEconomy.storage.MySQLStorage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StockMarketService {

    private final MySQLStorage storage;
    private final Map<String, Double> stocks;
    private final Random random;
    private final File stocksFile;
    private final FileConfiguration stocksConfig;

    public StockMarketService(MySQLStorage storage, File dataFolder) {
        this.storage = storage;
        this.stocks = new HashMap<>();
        this.random = new Random();
        this.stocksFile = new File(dataFolder, "stocks.yml");
        this.stocksConfig = YamlConfiguration.loadConfiguration(stocksFile);

        loadStocksFromConfig();
        initializeStocksFromDatabase();
    }

    private void loadStocksFromConfig() {
        if (!stocksFile.exists()) {
            // Add default stocks to config if file doesn't exist
            stocksConfig.set("stocks.Iron", 100.0);
            stocksConfig.set("stocks.Gold", 200.0);
            stocksConfig.set("stocks.Diamond", 500.0);
            try {
                stocksConfig.save(stocksFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Map<String, Double> getStocks() {
        return stocks;
    }

    private void initializeStocksFromDatabase() {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT stock_name, value FROM stocks");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stocks.put(rs.getString("stock_name"), rs.getDouble("value"));
            }

            for (String stockName : stocksConfig.getConfigurationSection("stocks").getKeys(false)) {
                if (!stocks.containsKey(stockName)) {
                    double value = stocksConfig.getDouble("stocks." + stockName);
                    addStock(stockName, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new stock to the database and memory.
     *
     * @param stockName The name of the stock.
     * @param value     The initial value of the stock.
     */
    public void addStock(String stockName, double value) {
        stocks.put(stockName, value);
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO stocks (stock_name, value) VALUES (?, ?)");
            stmt.setString(1, stockName);
            stmt.setDouble(2, value);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all stock data to the stocks.yml file.
     */
    public void saveStocksToConfig() {
        for (Map.Entry<String, Double> entry : stocks.entrySet()) {
            stocksConfig.set("stocks." + entry.getKey(), entry.getValue());
        }
        saveConfig();
    }

    private void saveConfig() {
        try {
            stocksConfig.save(stocksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the player's current balance from the database.
     *
     * @param player The player whose balance to retrieve.
     * @return The player's balance, or 0.0 if not found.
     */
    public double getPlayerBalance(Player player) {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM player_balances WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Sets the player's balance in the database.
     *
     * @param player The player whose balance to set.
     * @param amount The new balance.
     */
    public void setPlayerBalance(Player player, double amount) {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setDouble(2, amount);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the stock market to the player.
     *
     * @param player The player to show the stock market to.
     */
    public void showStockMarket(Player player) {
        player.sendMessage("=== Stock Market ===");
        stocks.forEach((name, value) -> player.sendMessage(name + ": $" + String.format("%.2f", value)));
    }

    /**
     * Gets the current value of a specific stock.
     *
     * @param stockName The name of the stock.
     * @return The stock value, or 0.0 if not found.
     */
    public double getStockValue(String stockName) {
        return stocks.getOrDefault(stockName, 0.0);
    }

    /**
     * Updates the value of a specific stock in memory and the database.
     *
     * @param stockName The name of the stock.
     * @param newValue  The new value of the stock.
     */
    public void updateStockValue(String stockName, double newValue) {
        if (stocks.containsKey(stockName)) {
            stocks.put(stockName, newValue);
            try (Connection conn = storage.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE stocks SET value = ? WHERE stock_name = ?");
                stmt.setDouble(1, newValue);
                stmt.setString(2, stockName);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows a player to buy stocks.
     *
     * @param player    The player buying the stock.
     * @param stockName The name of the stock to buy.
     * @param quantity  The quantity of the stock to buy.
     * @return True if the purchase was successful, false otherwise.
     */
    public boolean buyStock(Player player, String stockName, int quantity) {
        double stockPrice = getStockValue(stockName);
        if (stockPrice == 0.0) {
            player.sendMessage("Stock not found!");
            return false;
        }

        double totalCost = stockPrice * quantity;
        if (getPlayerBalance(player) >= totalCost) {
            setPlayerBalance(player, getPlayerBalance(player) - totalCost);
            player.sendMessage("You bought " + quantity + " of " + stockName + " stocks for $" + totalCost);
            return true;
        } else {
            player.sendMessage("You do not have enough balance to complete this purchase.");
            return false;
        }
    }

    /**
     * Allows a player to sell stocks.
     *
     * @param player    The player selling the stock.
     * @param stockName The name of the stock to sell.
     * @param quantity  The quantity of the stock to sell.
     * @return True if the sale was successful, false otherwise.
     */
    public boolean sellStock(Player player, String stockName, int quantity) {
        double stockPrice = getStockValue(stockName);
        if (stockPrice == 0.0) {
            player.sendMessage("Stock not found!");
            return false;
        }

        double totalValue = stockPrice * quantity;
        setPlayerBalance(player, getPlayerBalance(player) + totalValue);
        player.sendMessage("You sold " + quantity + " of " + stockName + " stocks for $" + totalValue);
        return true;
    }

    /**
     * Randomly fluctuates stock prices and updates them in the database.
     * This method should be called periodically (e.g., with a scheduler).
     */
    public void fluctuateStockPrices() {
        stocks.forEach((stock, price) -> {
            double change = (random.nextDouble() * 10 - 5);
            double newValue = Math.max(price + change, 1.0);
            updateStockValue(stock, newValue);
        });
    }
}
