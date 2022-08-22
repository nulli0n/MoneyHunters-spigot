package su.nightexpress.moneyhunters.basic.data;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserManager;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;

import java.util.UUID;

public class MoneyUserManager extends AbstractUserManager<MoneyHunters, MoneyUser> {

    public MoneyUserManager(@NotNull MoneyHunters plugin) {
        super(plugin, plugin);
    }

    @Override
    protected @NotNull MoneyUser createData(@NotNull UUID uuid, @NotNull String name) {
        return new MoneyUser(plugin, uuid, name);
    }

    @Override
    protected void onSynchronize() {
        // TODO
    }
}
