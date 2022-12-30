package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;

import java.util.List;
import java.util.Map;

public class ObjectivesCommand extends AbstractCommand<MoneyHunters> {

    public ObjectivesCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"objectives"}, Perms.COMMAND_OBJECTIVES);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_OBJECTIVES_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_OBJECTIVES_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getJobManager().getJobIds(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length < 2) {
            this.printUsage(sender);
            return;
        }

        IJob<?> job = plugin.getJobManager().getJobById(args[1]);
        if (job == null) {
            plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
            return;
        }

        Player player = (Player) sender;
        if (!job.hasPermission(player)) {
            this.errorPermission(sender);
            return;
        }

        job.getObjectivesMenu().open(player, 1);
    }
}
