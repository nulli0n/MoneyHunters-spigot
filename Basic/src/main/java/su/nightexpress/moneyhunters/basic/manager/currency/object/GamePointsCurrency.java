package su.nightexpress.moneyhunters.basic.manager.currency.object;

import su.nightexpress.moneyhunters.basic.MoneyHunters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.gamepoints.GamePoints;
import su.nightexpress.gamepoints.data.PointUser;
import su.nightexpress.moneyhunters.basic.api.currency.AbstractCurrency;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyId;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyManager;

public class GamePointsCurrency extends AbstractCurrency {

    private final GamePoints gamePoints;

    public GamePointsCurrency(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, CurrencyManager.DIR_CURRENCIES + CurrencyId.GAME_POINTS + ".yml"));
        this.gamePoints = GamePoints.getPlugin(GamePoints.class);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        PointUser user = gamePoints.getUserManager().getUserData(player);
        return user.getBalance();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        PointUser user = gamePoints.getUserManager().getUserData(player);
        user.addPoints((int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        PointUser user = gamePoints.getUserManager().getUserData(player);
        user.takePoints((int) amount);
    }
}
