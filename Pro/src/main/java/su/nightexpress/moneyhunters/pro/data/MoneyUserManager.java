package su.nightexpress.moneyhunters.pro.data;

import su.nightexpress.moneyhunters.pro.MoneyHunters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserManager;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;

public class MoneyUserManager extends AbstractUserManager<MoneyHunters, MoneyUser> {

    public MoneyUserManager(@NotNull MoneyHunters plugin) {
        super(plugin, plugin);
    }

    @Override
    @NotNull
    protected MoneyUser createData(@NotNull Player player) {
        return new MoneyUser(plugin, player);
    }
}
