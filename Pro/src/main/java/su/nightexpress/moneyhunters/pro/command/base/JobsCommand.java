package su.nightexpress.moneyhunters.pro.command.base;

import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;

public class JobsCommand extends AbstractCommand<MoneyHunters> {

    public JobsCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"jobs"}, Perms.COMMAND_JOBS);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_Jobs_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        plugin.getJobManager().getJobListMenu().open(player, 1);
    }
}
