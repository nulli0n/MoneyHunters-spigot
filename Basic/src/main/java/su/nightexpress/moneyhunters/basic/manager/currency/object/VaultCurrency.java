package su.nightexpress.moneyhunters.basic.manager.currency.object;

import su.nightexpress.moneyhunters.basic.MoneyHunters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.moneyhunters.basic.api.currency.AbstractCurrency;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyId;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyManager;

public class VaultCurrency extends AbstractCurrency {

    public VaultCurrency(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, CurrencyManager.DIR_CURRENCIES + CurrencyId.VAULT + ".yml"));
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return VaultHook.getBalance(player);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        VaultHook.addMoney(player, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        VaultHook.takeMoney(player, amount);
    }
}
