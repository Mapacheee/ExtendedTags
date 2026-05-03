package me.mapacheee.extendedtags.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

@Service
public final class EconomyService {

    private final Logger logger;
    private final boolean available;
    private Economy economy;

    @Inject
    public EconomyService(Logger logger, Plugin plugin) {
        this.logger = logger;
        this.available = setupEconomy();
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.info("Vault not found, economy features disabled.");
            return false;
        }
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                logger.info("No economy plugin found, economy features disabled.");
                return false;
            }
            economy = rsp.getProvider();
            logger.info("Economy enabled: {}", economy.getName());
            return true;
        } catch (Exception e) {
            logger.warn("Failed to setup economy", e);
            return false;
        }
    }

    public boolean isAvailable() {
        return available && economy != null;
    }

    public boolean has(Player player, double amount) {
        if (!isAvailable()) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!isAvailable()) return false;
        try {
            double balanceBefore = economy.getBalance(player);
            economy.withdrawPlayer(player, amount);
            double balanceAfter = economy.getBalance(player);
            return balanceAfter < balanceBefore;
        } catch (Exception e) {
            logger.error("Failed to withdraw money from {}", player.getName(), e);
            return false;
        }
    }

    public String format(double amount) {
        if (!isAvailable()) return "N/A";
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }
}