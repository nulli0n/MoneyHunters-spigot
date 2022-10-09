package su.nightexpress.moneyhunters.pro.manager.currency.object;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.gamepoints.api.GamePointsAPI;
import su.nightexpress.gamepoints.data.PointUser;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.currency.AbstractCurrency;
import su.nightexpress.moneyhunters.pro.manager.currency.CurrencyId;
import su.nightexpress.moneyhunters.pro.manager.currency.CurrencyManager;

public class GamePointsCurrency extends AbstractCurrency {

    public GamePointsCurrency(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, CurrencyManager.DIR_CURRENCIES + CurrencyId.GAME_POINTS + ".yml"));
    }

    @Override
    public double getBalance(@NotNull Player player) {
        PointUser user = GamePointsAPI.getUserData(player);
        return user.getBalance();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        PointUser user = GamePointsAPI.getUserData(player);
        user.addPoints((int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        PointUser user = GamePointsAPI.getUserData(player);
        user.takePoints((int) amount);
    }
}
