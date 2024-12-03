package org.ch4rlesexe.cEconomy.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.ch4rlesexe.cEconomy.models.StockMarketGUI;

public class StockMarketClickListener implements Listener {

    private final StockMarketGUI stockMarketGUI;

    public StockMarketClickListener(StockMarketGUI stockMarketGUI) {
        this.stockMarketGUI = stockMarketGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        stockMarketGUI.handleClick(event);
    }
}
