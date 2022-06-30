package su.nightexpress.moneyhunters.basic.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.Perms;
import su.nightexpress.moneyhunters.basic.api.job.IJob;
import su.nightexpress.moneyhunters.basic.config.Lang;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;

import java.util.Arrays;
import java.util.List;

public class LevelCommand extends GeneralCommand<MoneyHunters> {

    public LevelCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"level"}, Perms.COMMAND_LEVEL);
        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        this.addChildren(new SubCommand(plugin, new String[]{"add"}, Mode.ADD));
        this.addChildren(new SubCommand(plugin, new String[]{"take"}, Mode.TAKE));
        this.addChildren(new SubCommand(plugin, new String[]{"set"}, Mode.SET));
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_LEVEL_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }

    private enum Mode {
        ADD, TAKE, SET
    }

    private static class SubCommand extends AbstractCommand<MoneyHunters> {

        private final Mode mode;

        public SubCommand(@NotNull MoneyHunters plugin, @NotNull String[] aliases, @NotNull Mode mode) {
            super(plugin, aliases, Perms.COMMAND_LEVEL);
            this.mode = mode;
        }

        @Override
        @NotNull
        public String getUsage() {
            return switch (this.mode) {
                case ADD -> plugin.getMessage(Lang.COMMAND_LEVEL_ADD_USAGE).getLocalized();
                case TAKE -> plugin.getMessage(Lang.COMMAND_LEVEL_TAKE_USAGE).getLocalized();
                case SET -> plugin.getMessage(Lang.COMMAND_LEVEL_SET_USAGE).getLocalized();
            };
        }

        @Override
        @NotNull
        public String getDescription() {
            return switch (this.mode) {
                case ADD -> plugin.getMessage(Lang.COMMAND_LEVEL_ADD_DESC).getLocalized();
                case TAKE -> plugin.getMessage(Lang.COMMAND_LEVEL_TAKE_DESC).getLocalized();
                case SET -> plugin.getMessage(Lang.COMMAND_LEVEL_SET_DESC).getLocalized();
            };
        }

        @Override
        @NotNull
        public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
            if (arg == 2) {
                return PlayerUtil.getPlayerNames();
            }
            if (arg == 3) {
                return plugin.getJobManager().getJobIds();
            }
            if (arg == 4) {
                return Arrays.asList("1", "5", "10");
            }
            return super.getTab(player, arg, args);
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length < 5) {
                this.printUsage(sender);
                return;
            }

            IJob<?> job = plugin.getJobManager().getJobById(args[3]);
            if (job == null) {
                plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
                return;
            }

            int level = Math.abs(StringUtil.getInteger(args[4], 0));
            if (level == 0) {
                this.errorNumber(sender, args[4]);
                return;
            }

            MoneyUser user = plugin.getUserManager().getOrLoadUser(args[2], false);
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            UserJobData jobData = user.getJobData(job);
            LangMessage message = switch (this.mode) {
                case ADD -> {
                    user.addJobLevel(job, level);
                    yield plugin.getMessage(Lang.COMMAND_LEVEL_ADD_DONE);
                }
                case TAKE -> {
                    user.addJobLevel(job, -level);
                    yield plugin.getMessage(Lang.COMMAND_LEVEL_TAKE_DONE);
                }
                case SET -> {
                    jobData.setJobLevel(level);
                    yield plugin.getMessage(Lang.COMMAND_LEVEL_SET_DONE);
                }
            };
            jobData.update();

            message
                .replace(jobData.replacePlaceholders())
                .replace("%player%", user.getName())
                .replace("%amount%", level)
                .send(sender);
        }
    }
}
