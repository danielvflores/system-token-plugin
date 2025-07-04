package com.stp.enchants.impl;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.stp.core.SystemTokenEnchant;
import com.stp.enchants.CustomEnchant;
import com.stp.utils.MessageUtils;

import net.milkbowl.vault.economy.Economy;

public class GiveMoney implements CustomEnchant {
    private final int level;
    private final String displayName;
    private final int maxLevel;
    private final boolean enabled;
    private final int priceForLevel;
    private final boolean messageStatus;

    private static Economy economy = null;

    public GiveMoney(int level) {
        this.level = level;
        this.displayName = SystemTokenEnchant.getInstance().getConfig()
                .getString("enchants.givemoney.display", "Recolector de dinero");
        this.maxLevel = SystemTokenEnchant.getInstance().getConfig()
                .getInt("enchants.givemoney.max-level", 3);
        this.enabled = SystemTokenEnchant.getInstance().getConfig()
                .getBoolean("enchants.givemoney.enabled", true);
        this.priceForLevel = SystemTokenEnchant.getInstance().getConfig()
                .getInt("enchants.givemoney.price-for-level", 10);
        this.messageStatus = SystemTokenEnchant.getInstance().getConfig()
                .getBoolean("enchants.givemoney.messageStatus", true);

        if (economy == null) {
            if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
                economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            }
        }
    }

    @Override
    public String getId() {
        return "givemoney";
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void onEnable(Player player, int level) {}

    @Override
    public void onDisable(Player player) {}

    public void handleBlockBreak(Player player, ItemStack item) {
        if (!enabled) return;
        if (item == null || !item.getType().toString().contains("PICKAXE")) return;
        if (economy == null) return;

        double moneyToGive = level * priceForLevel;
        economy.depositPlayer(player, moneyToGive);

        if (messageStatus) {
            player.sendMessage(MessageUtils.getMessage(
                "give-money.received",
                "%money%", String.valueOf(moneyToGive),
                "%enchant%", getDisplayNamePlain()
            ));
        }
    }

    private String getDisplayNamePlain() {
        String colored = displayName.replace("&", "§");
        return colored.replaceAll("§[0-9a-fk-or]", "");
    }

    @Override
    public void applyEffect(Player player, int level) {
        if (!enabled) return;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        if (!enabled || item == null) return false;

        List<String> allowedTypes = SystemTokenEnchant.getInstance().getConfig()
            .getStringList("enchants." + getId() + ".enchants-item-avaible");
        boolean strict = SystemTokenEnchant.getInstance().getConfig()
            .getBoolean("enchants." + getId() + ".enchant-strict", false);

        String typeName = item.getType().name();

        boolean typeAllowed = false;
        for (String allowedType : allowedTypes) {
            if (typeName.endsWith(allowedType)) {
                typeAllowed = true;
                break;
            }
        }
        if (!typeAllowed) return false;

        if (strict) {

            String requiredName = SystemTokenEnchant.getInstance().getConfig()
                .getString("pickaxe.display-name", "");
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
            String displayName = item.getItemMeta().getDisplayName();

            return displayName.equals(requiredName.replace("&", "§"));
        }

        return true;
    }
}