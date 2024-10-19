package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PointsManager;
import de.janoschbl.cozy.utils.MessageUtils;
import de.janoschbl.cozy.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.io.IOException;
import java.util.*;

public class CozyBanCommand extends Command implements TabExecutor {

    private final List<String> subCommands = Arrays.asList(
            "enable", "disable", "reload",
            "setpoints", "getpoints", "help"
    );

    public CozyBanCommand() {
        super("cozyban", "cozyban.use");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cozyban.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessage("no-permission")));
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "enable":
                enablePlugin(sender);
                break;
            case "disable":
                disablePlugin(sender);
                break;
            case "reload":
                try {
                    reloadConfig(sender);
                } catch (IOException e) {
                    sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("reload-config-error")));
                    Main.getInstance().getLogger().warning(e.toString());
                }
                break;
            case "setpoints":
                setPoints(sender, args);
                break;
            case "getpoints":
                getPoints(sender, args);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }
    }

    // Subcommand Implementierungen

    private void enablePlugin(CommandSender sender) {
        Main.getInstance().setPluginEnabled(true);
        sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("enable-success")));
    }

    private void disablePlugin(CommandSender sender) {
        Main.getInstance().setPluginEnabled(false);
        sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("disable-success")));
    }

    private void reloadConfig(CommandSender sender) throws IOException {
        Main.getInstance().reloadConfig();
        sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("reload-success")));
    }

    private void setPoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cozyban.adminpoints.set")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }

        if (args.length != 3) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("points-usage-set")));
            return;
        }

        String target = args[1];
        String pointsStr = args[2];
        int points;

        try {
            points = Integer.parseInt(pointsStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("points-set-invalid")));
            return;
        }

        PointsManager.setPoints(UUIDFetcher.getUUID(target).toString(), points);
        sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("setpoints-success")
                .replace("%player%", target)
                .replace("%points%", String.valueOf(points))));
    }

    private void getPoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cozyban.adminpoints.get")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("points-usage-get")));
            return;
        }

        String target = args[1];
        String uuid = String.valueOf(UUIDFetcher.getUUID(target));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("points-get-invalid")));
            return;
        }

        Integer points = PointsManager.getTotalPoints(uuid);
        if (points == null) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("points-get-invalid")));
            return;
        }

        sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("getpoints-success")
                .replace("%player%", target)
                .replace("%points%", String.valueOf(points))));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-header")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-enable")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-disable")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-reload")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-setpoints")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-getpoints")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-help")));
        sender.sendMessage(new TextComponent(MessageUtils.getMessage("help-footer")));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cozyban.use")) {
            return Collections.emptySet();
        }

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String cmd : subCommands) {
                if (cmd.startsWith(prefix)) {
                    suggestions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (Arrays.asList("setpoints", "getpoints").contains(subCommand)) {
                String prefix = args[1].toLowerCase();
                for (ProxiedPlayer player : Main.getInstance().getProxy().getPlayers()) {
                    String name = player.getName();
                    if (name.toLowerCase().startsWith(prefix)) {
                        suggestions.add(name);
                    }
                }
            }
        }

        else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (Objects.equals("setpoints", subCommand)) {
                String prefix = args[2];
                for (int i = 0; i <= 1000; i += 10) { // Beispielvorschläge alle 10 Punkte
                    String pointStr = String.valueOf(i);
                    if (pointStr.startsWith(prefix)) {
                        suggestions.add(pointStr);
                    }
                }
            }
        }

        return suggestions;
    }
}
