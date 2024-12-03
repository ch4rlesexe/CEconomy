package org.ch4rlesexe.cEconomy.services;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ch4rlesexe.cEconomy.storage.MySQLStorage;

import java.util.HashMap;
import java.util.Map;

public class ShopService {
    private final MySQLStorage storage;
    private final Map<String, Double> shopPrices = new HashMap<>();

    public ShopService(MySQLStorage storage) {
        this.storage = storage;
        loadShops();
    }

    private void loadShops() {
        // Load shop items from database or config
        shopPrices.put("Diamond", 500.0);
        shopPrices.put("Iron", 100.0);
    }

    public boolean buyItem(Player player, String itemName, int quantity) {
        double totalCost = shopPrices.getOrDefault(itemName, 0.0) * quantity;
        BankingService bankingService = new BankingService(storage);
        if (bankingService.getBalance(player) >= totalCost) {
            bankingService.withdraw(player, totalCost);
            player.getInventory().addItem(new ItemStack(org.bukkit.Material.valueOf(itemName.toUpperCase()), quantity));
            return true;
        }
        return false;
    }

    public boolean sellItem(Player player, String itemName, int quantity) {
        double totalValue = shopPrices.getOrDefault(itemName, 0.0) * quantity;
        player.getInventory().removeItem(new ItemStack(org.bukkit.Material.valueOf(itemName.toUpperCase()), quantity));
        BankingService bankingService = new BankingService(storage);
        bankingService.deposit(player, totalValue);
        return true;
    }
}
