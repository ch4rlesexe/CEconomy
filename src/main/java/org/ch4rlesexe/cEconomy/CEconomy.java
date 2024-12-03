package org.ch4rlesexe.cEconomy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.ch4rlesexe.cEconomy.commands.EconomyCommand;
import org.ch4rlesexe.cEconomy.events.BlockBreakListener;
import org.ch4rlesexe.cEconomy.events.StockMarketClickListener;
import org.ch4rlesexe.cEconomy.models.StockMarketGUI;
import org.ch4rlesexe.cEconomy.services.BankingService;
import org.ch4rlesexe.cEconomy.services.ShopService;
import org.ch4rlesexe.cEconomy.services.StockMarketService;
import org.ch4rlesexe.cEconomy.storage.MySQLStorage;

import java.io.File;
import java.io.IOException;

public class CEconomy extends JavaPlugin {

    private static CEconomy instance;
    private StockMarketService stockMarketService;
    private BankingService bankingService;
    private ShopService shopService;
    private MySQLStorage mySQLStorage;

    @Override
    public void onEnable() {
        instance = this;

        // Load database configuration
        File dbFile = new File(getDataFolder(), "database.yml");
        FileConfiguration dbConfig = YamlConfiguration.loadConfiguration(dbFile);
        if (!dbFile.exists()) {
            setupDefaultDatabaseConfig(dbConfig, dbFile);
        }

        // Initialize MySQLStorage
        mySQLStorage = new MySQLStorage(
                dbConfig.getString("host"),
                dbConfig.getInt("port"),
                dbConfig.getString("database"),
                dbConfig.getString("username"),
                dbConfig.getString("password")
        );
        mySQLStorage.connect();

        // Initialize services
        stockMarketService = new StockMarketService(mySQLStorage, getDataFolder());
        bankingService = new BankingService(mySQLStorage);
        shopService = new ShopService(mySQLStorage);

        // Initialize GUI
        StockMarketGUI stockMarketGUI = new StockMarketGUI(stockMarketService);

        // Register commands
        getCommand("economy").setExecutor(new EconomyCommand(stockMarketService, bankingService, shopService, stockMarketGUI));

        // Register events
        getServer().getPluginManager().registerEvents(new BlockBreakListener(stockMarketService), this);
        getServer().getPluginManager().registerEvents(new StockMarketClickListener(stockMarketGUI), this);

        saveDefaultConfig();

        getLogger().info("CEconomy plugin enabled!");
    }

    @Override
    public void onDisable() {
        stockMarketService.saveStocksToConfig();
        mySQLStorage.disconnect();
        getLogger().info("CEconomy plugin disabled!");
    }

    private void setupDefaultDatabaseConfig(FileConfiguration dbConfig, File dbFile) {
        dbConfig.set("host", "localhost");
        dbConfig.set("port", 3306);
        dbConfig.set("database", "economy");
        dbConfig.set("username", "root");
        dbConfig.set("password", "password");
        try {
            dbConfig.save(dbFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CEconomy getInstance() {
        return instance;
    }

    public StockMarketService getStockMarketService() {
        return stockMarketService;
    }

    public BankingService getBankingService() {
        return bankingService;
    }

    public ShopService getShopService() {
        return shopService;
    }
}
