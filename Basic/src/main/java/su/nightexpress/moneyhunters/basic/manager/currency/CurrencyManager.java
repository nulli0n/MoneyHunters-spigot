package su.nightexpress.moneyhunters.basic.manager.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.currency.ICurrency;
import su.nightexpress.moneyhunters.basic.manager.currency.object.VaultCurrency;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CurrencyManager extends AbstractManager<MoneyHunters> {

    private Map<String, ICurrency> currencyMap;

    public static final String DIR_CURRENCIES = "/currency/";

    public CurrencyManager(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_CURRENCIES);
        this.currencyMap = new HashMap<>();
        this.setupDefaults();
    }

    @Override
    public void onShutdown() {
        if (this.currencyMap != null) {
            this.currencyMap.clear();
        }
    }

    private void setupDefaults() {
        if (VaultHook.hasEconomy()) {
            this.registerCurrency(new VaultCurrency(this.plugin));
        }
    }

    public void registerCurrency(@NotNull ICurrency currency) {
        this.currencyMap.put(currency.getId(), currency);
        this.plugin.info("Registered currency: " + currency.getId() + " (" + currency.getName() + ")");
    }

    public boolean hasCurrency() {
        return !this.currencyMap.isEmpty();
    }

    @NotNull
    public Collection<ICurrency> getCurrencies() {
        return this.currencyMap.values();
    }

    @Nullable
    public ICurrency getCurrency(@NotNull String id) {
        return this.currencyMap.get(id.toLowerCase());
    }
}
