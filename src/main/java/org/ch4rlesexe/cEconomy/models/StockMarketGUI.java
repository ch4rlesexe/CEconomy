package org.ch4rlesexe.cEconomy.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ch4rlesexe.cEconomy.services.StockMarketService;

import java.util.Map;

public class StockMarketGUI {

    private final StockMarketService stockMarketService;

    public StockMarketGUI(StockMarketService stockMarketService) {
        this.stockMarketService = stockMarketService;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Stock Market");

        int slot = 0;
        for (Map.Entry<String, Double> stock : stockMarketService.getStocks().entrySet()) {
            ItemStack stockItem = new ItemStack(Material.EMERALD);
            ItemMeta meta = stockItem.getItemMeta();
            meta.setDisplayName("§a" + stock.getKey());
            meta.setLore(java.util.Arrays.asList("§7Value: §6$" + String.format("%.2f", stock.getValue())));
            stockItem.setItemMeta(meta);
            gui.setItem(slot++, stockItem);
        }

        player.openInventory(gui);
    }

    public void handleClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Stock Market")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        String stockName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2); // Remove "§a"
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("§eYou selected: " + stockName);
    }
}
