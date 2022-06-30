package su.nightexpress.moneyhunters.pro.command.base;

import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.List;

public class StatsCommand extends AbstractCommand<MoneyHunters> {

    public StatsCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"stats"}, Perms.COMMAND_STATS);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_STATS_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_STATS_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return PlayerUtil.getPlayerNames();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        MoneyUser user = null;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                this.printUsage(sender);
                return;
            }
            user = plugin.getUserManager().getOrLoadUser(player);
        }
        else if (args.length == 2) {
            String pName = args[1];
            user = plugin.getUserManager().getOrLoadUser(pName, false);
        }

        if (user == null) {
            this.errorPlayer(sender);
            return;
        }

        MoneyUser finalUser = user;
        plugin.getMessage(Lang.COMMAND_STATS_DISPLAY).asList().forEach(line -> {
            if (line.contains("job_")) {
                finalUser.getJobData().values().forEach(progress -> {
                    String line2 = progress.replacePlaceholders().apply(line);
                    MessageUtil.sendWithJSON(sender, line2);
                });
                return;
            }

            MessageUtil.sendWithJSON(sender, line);
        });
    }
}
