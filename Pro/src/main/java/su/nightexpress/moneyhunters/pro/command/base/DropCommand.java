package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.config.Lang;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DropCommand extends AbstractCommand<MoneyHunters> {

    public DropCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"drop"}, Perms.COMMAND_DROP);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_DROP_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_DROP_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getCurrencyManager().getCurrencies().stream().map(ICurrency::getId).toList();
        }
        if (arg == 2) {
            return Arrays.asList("10", "100", "100:500");
        }
        if (arg == 3) {
            return LocationUtil.getWorldNames();
        }
        if (arg == 4) {
            return Arrays.asList("<x>", NumberUtil.format(player.getLocation().getX()));
        }
        if (arg == 5) {
            return Arrays.asList("<y>", NumberUtil.format(player.getLocation().getY()));
        }
        if (arg == 6) {
            return Arrays.asList("<z>", NumberUtil.format(player.getLocation().getZ()));
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length != 7) {
            this.printUsage(sender);
            return;
        }

        ICurrency currency = plugin.getCurrencyManager().getCurrency(args[1]);
        if (currency == null) {
            plugin.getMessage(Lang.CURRENCY_ERROR_INVALID).send(sender);
            return;
        }

        World world = plugin.getServer().getWorld(args[3]);
        if (world == null) {
            plugin.getMessage(EngineLang.ERROR_WORLD_INVALID).send(sender);
            return;
        }

        String[] range = args[2].split(":");
        double amount1 = StringUtil.getDouble(range[0], 0);
        double amount2 = range.length > 1 ? StringUtil.getDouble(range[1], 0) : amount1;

        double amount = Rnd.getDouble(amount1, amount2);
        if (amount <= 0) return;

        ItemStack item = currency.createMoney(amount, null, null, null);

        double x = StringUtil.getDouble(args[4], 0, true);
        double y = StringUtil.getDouble(args[5], 0, true);
        double z = StringUtil.getDouble(args[6], 0, true);

        Location loc = new Location(world, x, y, z);
        world.dropItem(loc, item);

        plugin.getMessage(Lang.COMMAND_DROP_DONE)
            .replace(Placeholders.GENERIC_MONEY, currency.format(amount))
            .replace("%x%", NumberUtil.format(x))
            .replace("%y%", NumberUtil.format(y))
            .replace("%z%", NumberUtil.format(z))
            .replace("%world%", LocationUtil.getWorldName(world))
            .send(sender);
    }
}
