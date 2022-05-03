package su.nightexpress.moneyhunters.basic.command.booster;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.Perms;

public class BoosterCommand extends GeneralCommand<MoneyHunters> {

    public BoosterCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"booster"}, Perms.COMMAND_BOOSTER);
        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        this.addChildren(new BoosterGlobalCommand(plugin));
        this.addChildren(new BoosterPersonalCommand(plugin));
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_Booster_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {

    }
}
