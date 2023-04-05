package su.nightexpress.moneyhunters.pro.manager.leaderboard.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram.LeaderboardHologramHandler;

import java.util.List;
import java.util.Map;

public class LeaderboardHologramCommand extends GeneralCommand<MoneyHunters> {

    private final LeaderboardHologramHandler hologramHandler;

    public LeaderboardHologramCommand(@NotNull MoneyHunters plugin, @NotNull LeaderboardHologramHandler hologramHandler) {
        super(plugin, new String[]{"hologram"}, Perms.COMMAND_LEADERBOARD_HOLOGRAM);
        this.hologramHandler = hologramHandler;
        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        this.addChildren(new AddCommand(plugin));
        this.addChildren(new RemoveCommand(plugin));
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings, @NotNull Map<String, String> flags) {

    }

    class AddCommand extends AbstractCommand<MoneyHunters> {

        public AddCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"add"}, Perms.COMMAND_LEADERBOARD_HOLOGRAM);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_ADD_USAGE).getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_ADD_DESC).getLocalized();
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        @NotNull
        public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
            if (arg == 3) {
                return CollectionsUtil.getEnumsList(LeaderboardType.class);
            }
            if (arg == 4) {
                return plugin.getJobManager().getJobIds();
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
            if (args.length != 5) {
                this.printUsage(sender);
                return;
            }

            LeaderboardType boardType = CollectionsUtil.getEnum(args[3], LeaderboardType.class);
            if (boardType == null) {
                this.printUsage(sender);
                return;
            }

            IJob<?> job = plugin.getJobManager().getJobById(args[4]);
            if (job == null) {
                plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
                return;
            }

            Player player = (Player) sender;
            hologramHandler.createHologram(boardType, job.getId(), player.getLocation());

            plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_ADD_DONE)
                .replace(LeaderboardManager.PLACEHOLDER_TYPE, boardType.name())
                .replace(LeaderboardManager.PLACEHOLDER_JOB, job.getName())
                .send(player);
        }
    }

    class RemoveCommand extends AbstractCommand<MoneyHunters> {

        public RemoveCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"remove"}, Perms.COMMAND_LEADERBOARD_HOLOGRAM);
        }

        @Override
        @NotNull
        public String getUsage() {
            return "";
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_REMOVE_DESC).getLocalized();
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
            Player player = (Player) sender;
            hologramHandler.removeHologram(player.getLocation());
            plugin.getMessage(Lang.COMMAND_LEADERBOARD_HOLOGRAM_REMOVE_DONE).send(sender);
        }
    }
}
