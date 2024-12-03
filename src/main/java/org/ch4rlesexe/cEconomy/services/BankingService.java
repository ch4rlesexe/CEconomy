package org.ch4rlesexe.cEconomy.services;

import org.bukkit.entity.Player;
import org.ch4rlesexe.cEconomy.storage.MySQLStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BankingService {
    private final MySQLStorage storage;

    public BankingService(MySQLStorage storage) {
        this.storage = storage;
    }

    public double getBalance(Player player) {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM accounts WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void deposit(Player player, double amount) {
        double currentBalance = getBalance(player);
        setBalance(player, currentBalance + amount);
    }

    public void withdraw(Player player, double amount) {
        double currentBalance = getBalance(player);
        if (currentBalance >= amount) setBalance(player, currentBalance - amount);
    }

    public void setBalance(Player player, double balance) {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO accounts (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setDouble(2, balance);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void accrueInterest() {
        try (Connection conn = storage.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET balance = balance * 1.01");
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
