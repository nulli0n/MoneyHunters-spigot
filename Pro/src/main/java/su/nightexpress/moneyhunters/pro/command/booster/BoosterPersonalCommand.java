package su.nightexpress.moneyhunters.pro.command.booster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.manager.booster.object.PersonalBooster;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoosterPersonalCommand extends GeneralCommand<MoneyHunters> {

    public BoosterPersonalCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"personal"}, Perms.COMMAND_BOOSTER);
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
        return plugin.lang().Command_Booster_Personal_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }

    @NotNull
    private Set<MoneyUser> parseUsers(@NotNull String userName) {
        Set<MoneyUser> users = new HashSet<>();
        if (userName.equalsIgnoreCase(Constants.MASK_ANY)) {
            users.addAll(plugin.getServer().getOnlinePlayers().stream()
                .map(player -> plugin.getUserManager().getOrLoadUser(player))
                .collect(Collectors.toSet()));
        }
        else {
            MoneyUser user = plugin.getUserManager().getOrLoadUser(userName, false);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    class GiveSubCommand extends AbstractCommand<MoneyHunters> {

        public GiveSubCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"give"}, Perms.COMMAND_BOOSTER);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.lang().Command_Booster_Personal_Give_Usage.getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.lang().Command_Booster_Personal_Give_Desc.getLocalized();
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
                list.add(Constants.MASK_ANY);
                list.addAll(PlayerUtil.getPlayerNames());
                return list;
            }
            if (arg == 4) {
                List<String> list = new ArrayList<>();
                MoneyUser user = plugin.getUserManager().getOrLoadUser(args[3], false);
                if (user != null) {
                    list.addAll(user.getBoosters().stream()
                        .filter(booster -> booster.getType() == BoosterType.PERSONAL)
                        .map(IBooster::getId).toList());
                }

                list.add("<booster_id>");
                return list;
            }
            if (arg == 5) {
                List<String> list = new ArrayList<>();
                list.add("<jobId>");
                list.add("<job1,job2>");
                list.add(Constants.MASK_ANY);
                list.addAll(plugin.getJobManager().getJobIds());
                return list;
            }
            if (arg == 6) {
                return Arrays.asList("<money>", "1.5", "2.0", "3.0");
            }
            if (arg == 7) {
                return Arrays.asList("<exp>", "1.5", "2.0", "3.0");
            }
            if (arg == 8) {
                return Arrays.asList("<minutes>", "30", "60", "1440");
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length != 9) {
                this.printUsage(sender);
                return;
            }

            String id = args[4];
            String jobsRaw = args[5];

            Set<String> jobIds;
            if (jobsRaw.equalsIgnoreCase(Constants.MASK_ANY)) {
                jobIds = new HashSet<>(plugin.getJobManager().getJobIds());
            }
            else {
                jobIds = Stream.of(jobsRaw.split(","))
                    .filter(jobId -> plugin.getJobManager().getJobById(jobId) != null).collect(Collectors.toSet());
            }
            if (jobIds.isEmpty()) {
                plugin.lang().Job_Error_InvalidJob.send(sender);
                return;
            }

            double modMoney = StringUtil.getDouble(args[6], 1D);
            double modExp = StringUtil.getDouble(args[7], 1D);
            if (modMoney == 1D && modExp == 1D) {
                this.errorNumber(sender, args[6] + "/" + args[7]);
                return;
            }

            int duration = StringUtil.getInteger(args[8], 0);
            if (duration == 0) {
                this.errorNumber(sender, args[8]);
                return;
            }

            String userName = args[3];
            Set<MoneyUser> users = parseUsers(userName);
            if (users.isEmpty()) {
                this.errorPlayer(sender);
                return;
            }

            users.forEach(user -> {
                PersonalBooster booster = new PersonalBooster(id, jobIds, modMoney, modExp, duration);
                user.getBoosters().removeIf(boosterHas -> boosterHas.getId().equalsIgnoreCase(id));
                user.getBoosters().add(booster);

                Player target = user.getPlayer();
                if (target != null) {
                    plugin.lang().Booster_Personal_Notify
                        .replace(booster.replacePlaceholders())
                        .replace("%player%", user.getName())
                        .send(target);
                }

                plugin.lang().Command_Booster_Personal_Give_Done
                    .replace(booster.replacePlaceholders())
                    .replace("%player%", user.getName())
                    .send(sender);
            });
        }
    }

    class RemoveSubCommand extends AbstractCommand<MoneyHunters> {

        public RemoveSubCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"remove"}, Perms.COMMAND_BOOSTER);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.lang().Command_Booster_Personal_Remove_Usage.getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.lang().Command_Booster_Personal_Remove_Desc.getLocalized();
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
                list.add(Constants.MASK_ANY);
                list.addAll(PlayerUtil.getPlayerNames());
                return list;
            }
            if (arg == 4) {
                List<String> list = new ArrayList<>();
                list.add("<booster_id>");
                list.add(Constants.MASK_ANY);

                MoneyUser user = plugin.getUserManager().getOrLoadUser(args[3], false);
                if (user != null) {
                    list.addAll(user.getBoosters().stream()
                        .filter(booster -> booster.getType() == BoosterType.PERSONAL)
                        .map(IBooster::getId).toList());
                }
                return list;
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length != 5) {
                this.printUsage(sender);
                return;
            }

            String id = args[4];

            String userName = args[3];
            Set<MoneyUser> users = parseUsers(userName);
            if (users.isEmpty()) {
                this.errorPlayer(sender);
                return;
            }

            users.forEach(user -> {
                boolean removed = user.getBoosters().removeIf(boosterHas -> boosterHas.getId().equalsIgnoreCase(id) || id.equalsIgnoreCase(Constants.MASK_ANY));
                LangMessage msgNotify = removed ? plugin.lang().Command_Booster_Personal_Remove_Done : plugin.lang().Command_Booster_Personal_Remove_Error_Nothing;

                msgNotify
                    .replace(Placeholders.BOOSTER_ID, id)
                    .replace("%player%", user.getName())
                    .send(sender);
            });
        }
    }
}
