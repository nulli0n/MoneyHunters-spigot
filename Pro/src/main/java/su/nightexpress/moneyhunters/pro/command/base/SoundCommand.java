package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;

public class SoundCommand extends AbstractCommand<MoneyHunters> {

    public SoundCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"sound"}, null);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_Sound_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        MoneyUser user = plugin.getUserManager().getOrLoadUser(player);

        user.getSettings().setSoundPickupEnabled(!user.getSettings().isSoundPickupEnabled());
        plugin.lang().Command_Sound_Done
            .replace("%state%", plugin.lang().getBoolean(user.getSettings().isSoundPickupEnabled()))
            .send(player);
    }
}
