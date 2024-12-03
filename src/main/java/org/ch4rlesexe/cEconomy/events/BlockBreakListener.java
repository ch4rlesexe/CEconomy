package org.ch4rlesexe.cEconomy.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.ch4rlesexe.cEconomy.services.StockMarketService;

public class BlockBreakListener implements Listener {

    private final StockMarketService stockMarketService;

    public BlockBreakListener(StockMarketService stockMarketService) {
        this.stockMarketService = stockMarketService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();

        if (blockType == Material.IRON_ORE) {
            double currentValue = stockMarketService.getStockValue("Iron");
            stockMarketService.updateStockValue("Iron", currentValue - 1);
        } else if (blockType == Material.GOLD_ORE) {
            double currentValue = stockMarketService.getStockValue("Gold");
            stockMarketService.updateStockValue("Gold", currentValue - 1);
        }
    }
}
