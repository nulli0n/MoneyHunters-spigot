package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;

import java.util.Map;

public class SoundCommand extends AbstractCommand<MoneyHunters> {

    public SoundCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"sound"}, (String) null);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_SOUND_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        Player player = (Player) sender;
        MoneyUser user = plugin.getUserManager().getUserData(player);

        user.getSettings().setSoundPickupEnabled(!user.getSettings().isSoundPickupEnabled());
        plugin.getMessage(Lang.COMMAND_SOUND_DONE)
            .replace("%state%", LangManager.getBoolean(user.getSettings().isSoundPickupEnabled()))
            .send(player);
    }
}
