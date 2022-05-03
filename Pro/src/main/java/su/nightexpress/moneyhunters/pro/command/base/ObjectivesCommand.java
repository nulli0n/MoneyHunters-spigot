package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.job.IJob;

import java.util.List;

public class ObjectivesCommand extends AbstractCommand<MoneyHunters> {

    public ObjectivesCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"objectives"}, Perms.COMMAND_OBJECTIVES);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.lang().Command_Objectives_Usage.getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_Objectives_Desc.getLocalized();
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            this.printUsage(sender);
            return;
        }

        IJob<?> job = plugin.getJobManager().getJobById(args[1]);
        if (job == null) {
            plugin.lang().Job_Error_InvalidJob.send(sender);
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
