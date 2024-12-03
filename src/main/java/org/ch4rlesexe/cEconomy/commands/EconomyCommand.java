package org.ch4rlesexe.cEconomy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ch4rlesexe.cEconomy.models.StockMarketGUI;
import org.ch4rlesexe.cEconomy.services.BankingService;
import org.ch4rlesexe.cEconomy.services.ShopService;
import org.ch4rlesexe.cEconomy.services.StockMarketService;

public class EconomyCommand implements CommandExecutor {

    private final StockMarketService stockMarketService;
    private final BankingService bankingService;
    private final ShopService shopService;
    private final StockMarketGUI stockMarketGUI;

    public EconomyCommand(StockMarketService stockMarketService, BankingService bankingService, ShopService shopService, StockMarketGUI stockMarketGUI) {
        this.stockMarketService = stockMarketService;
        this.bankingService = bankingService;
        this.shopService = shopService;
        this.stockMarketGUI = stockMarketGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /economy <stocks|balance|shop>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stocks":
                stockMarketGUI.open(player);
                break;
            case "balance":
                double balance = bankingService.getBalance(player);
                player.sendMessage("Your balance: $" + balance);
                break;
            case "shop":
                player.sendMessage("Shop feature is under development.");
                break;
            default:
                player.sendMessage("Unknown subcommand.");
        }

        return true;
    }
}
