package su.nightexpress.moneyhunters.pro.command.booster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.manager.booster.object.GlobalBooster;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoosterGlobalCommand extends GeneralCommand<MoneyHunters> {

    public BoosterGlobalCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"global"}, Perms.COMMAND_BOOSTER);
        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        this.addChildren(new GiveSubCommand(plugin));
        this.addChildren(new RemoveSubCommand(plugin));
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }

    static class GiveSubCommand extends AbstractCommand<MoneyHunters> {

        public GiveSubCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"create"}, Perms.COMMAND_BOOSTER);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_CREATE_USAGE).getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_CREATE_DESC).getLocalized();
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        @NotNull
        public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
            if (arg == 3) {
                List<String> list = new ArrayList<>();
                list.add("<booster_id>");
                list.addAll(plugin.getBoosterManager().getBoostersGlobal().stream().map(IBooster::getId).toList());
                return list;
            }
            if (arg == 4) {
                List<String> list = new ArrayList<>();
                list.add("<jobId>");
                list.add("<job1,job2>");
                list.add(Placeholders.MASK_ANY);
                list.addAll(plugin.getJobManager().getJobIds());
                return list;
            }
            if (arg == 5) {
                return Arrays.asList("<money>", "1.5", "2.0", "3.0");
            }
            if (arg == 6) {
                return Arrays.asList("<exp>", "1.5", "2.0", "3.0");
            }
            if (arg == 7) {
                return Arrays.asList("<minutes>", "30", "60", "1440");
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length != 8) {
                this.printUsage(sender);
                return;
            }

            String boosterId = args[3];
            String jobsRaw = args[4];

            Set<String> jobIds;
            if (jobsRaw.equalsIgnoreCase(Placeholders.MASK_ANY)) {
                jobIds = new HashSet<>(plugin.getJobManager().getJobIds());
            }
            else {
                jobIds = Stream.of(jobsRaw.split(","))
                    .filter(jobId -> plugin.getJobManager().getJobById(jobId) != null).collect(Collectors.toSet());
            }
            if (jobIds.isEmpty()) {
                plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
                return;
            }

            double modMoney = StringUtil.getDouble(args[5], 1D);
            double modExp = StringUtil.getDouble(args[6], 1D);
            if (modMoney == 1D && modExp == 1D) {
                this.errorNumber(sender, args[5] + "/" + args[6]);
                return;
            }

            int duration = StringUtil.getInteger(args[7], 0);
            if (duration == 0) {
                this.errorNumber(sender, args[7]);
                return;
            }

            GlobalBooster booster = new GlobalBooster(boosterId, jobIds, modMoney, modExp, duration);
            plugin.getBoosterManager().getBoostersGlobal().removeIf(boosterHas -> boosterHas.getId().equalsIgnoreCase(boosterId));
            plugin.getBoosterManager().getBoostersGlobal().add(booster);
            plugin.getBoosterManager().updateBoosters();
            plugin.getBoosterManager().notifyBooster();

            plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_CREATE_DONE)
                .replace(booster.replacePlaceholders())
                .send(sender);
        }
    }

    static class RemoveSubCommand extends AbstractCommand<MoneyHunters> {

        public RemoveSubCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"remove"}, Perms.COMMAND_BOOSTER);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_REMOVE_USAGE).getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_REMOVE_DESC).getLocalized();
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        @NotNull
        public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
            if (arg == 3) {
                List<String> list = new ArrayList<>();
                list.add(Placeholders.MASK_ANY);
                list.addAll(plugin.getBoosterManager().getBoostersGlobal().stream().map(IBooster::getId).toList());
                return list;
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length != 4) {
                this.printUsage(sender);
                return;
            }

            String id = args[3];

            boolean removed = plugin.getBoosterManager().getBoostersGlobal().removeIf(boosterHas -> boosterHas.getId().equalsIgnoreCase(id) || id.equalsIgnoreCase(Placeholders.MASK_ANY));
            LangMessage msgNotify = removed ? plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_REMOVE_DONE) : plugin.getMessage(Lang.COMMAND_BOOSTER_GLOBAL_REMOVE_ERROR_NOTHING);
            msgNotify.replace(Placeholders.BOOSTER_ID, id).send(sender);

            plugin.getBoosterManager().updateBoosters();
        }
    }
}
