package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

import java.util.List;

public class InfoCommand extends AbstractCommand<MoneyHunters> {

    public InfoCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"info"}, Perms.COMMAND_INFO);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_INFO_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_INFO_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public @NotNull List<@NotNull String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getJobManager().getJobIds();
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
            plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
            return;
        }

        Player player = (Player) sender;
        MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
        UserJobData progress = user.getJobData(job);

        plugin.getMessage(Lang.COMMAND_INFO_DISPLAY).asList().forEach(line -> {
            line = progress.replacePlaceholders().apply(line);

            if (line.contains("%booster_")) {
                for (IBooster booster : user.getBoosters(job)) {
                    MessageUtil.sendWithJSON(player, booster.replacePlaceholders().apply(line));
                }
                return;
            }

            MessageUtil.sendWithJSON(player, line);
        });
    }
}
