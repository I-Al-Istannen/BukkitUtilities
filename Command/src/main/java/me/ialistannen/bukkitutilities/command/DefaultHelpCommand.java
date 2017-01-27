package me.ialistannen.bukkitutilities.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import me.ialistannen.bukkitutilities.language.MessageProvider;
import me.ialistannen.bukkitutilities.utilities.collections.ListUtils;
import me.ialistannen.bukkitutilities.utilities.text.JsonMessage;
import me.ialistannen.bukkitutilities.utilities.text.Pager;
import me.ialistannen.bukkitutilities.utilities.text.Pager.Options;
import me.ialistannen.bukkitutilities.utilities.text.Pager.Page;
import me.ialistannen.bukkitutilities.utilities.text.Pager.PagerFilterable;
import me.ialistannen.bukkitutilities.utilities.text.Pager.SearchMode;
import me.ialistannen.bukkitutilities.utilities.text.TextUtils;

import javax.annotation.Nonnull;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static me.ialistannen.bukkitutilities.utilities.text.TextUtils.colorize;

// @formatter:off I WANT THAT FORMATTING
/**
 * The default help command
 * <p>
 * <br>
 * <b>Language:</b>
 * Base key: "command.help"
 * <br>Needs all keys from {@link TranslatedCommandNode} 
 * <p>
 * Keys:
 * <ul>
 *     <li>"command.help.format.with.usage" {@code ==>} The help format with the usage.
 *       <br><b>Default:</b> <i>{@code "&3{0}&9: &7{1} &7<&6{2}&7><newline>  &cUsage: {3}"}</i>
 *       <br><b>Format parameters:</b>
 *       <ol>
 *           <li>Name</li>
 *           <li>Description</li>
 *           <li>Children amount</li>
 *           <li>Usage</li>
 *       </ol>
 *     </li>
 *     <li>"command.help.format.without.usage" {@code ==>} The help format without the usage.
 *       <br><b>Default: </b> <i>{@code "&3{0}&9: &7{1} &7<&6{2}&7>"}</i>
 *       <br><b>Format parameters:</b>
 *       <ol>
 *           <li>Name</li>
 *           <li>Description</li>
 *           <li>Children amount</li>
 *           <li>Usage</li>
 *       </ol>
 *     </li>
 *     <li>"command.help.top.level.prefix" {@code ==>} The prefix for a top level command
 *       <br><b>Default:</b> <i>Nothing</i>
 *     </li>
 *     <li>"command.help.sub.level.prefix" {@code ==>} The prefix for a sub level command
 *       <br><b>Default:</b> <i>Nothing</i>
 *     </li>
 *     <li>"command.help.padding.char"     {@code ==>} The padding char
 *       <br><b>Default:</b> <i>Space</i>
 *     </li>
 * </ul>
 */
// @formatter:on
public class DefaultHelpCommand extends TranslatedCommandNode {

    private OptionsParser parser = new OptionsParser();
    private CommandTree commandTree;

    /**
     * The base key will be "command.help". <br>
     * For all keys used by this command, look at the class javadoc:
     * {@link DefaultHelpCommand}
     *
     * @param permission The Permission to use
     * @param messageProvider The {@link MessageProvider} to use
     * @param commandTree The {@link CommandTree} to make it for
     */
    @SuppressWarnings("WeakerAccess")
    public DefaultHelpCommand(Permission permission, MessageProvider messageProvider, CommandTree commandTree) {
        super(permission, "command.help", messageProvider, CommandSenderType.ALL);
        this.commandTree = commandTree;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> wholeChat, int relativeIndex) {
        return Arrays.asList("--depth=", "--page=", "--entriesPerPage=", "--showUsage=",
                "--search=", "--regex=", "--explanation"); // , "--contains=", "--regexFind=",
        // "--regexMatch=", "--regexMatchCI="
    }

    @Override
    protected CommandResult executeGeneral(CommandSender sender, String... args) {
        Options options = parser.parse(args);
        AtomicInteger depth = new AtomicInteger(10);
        AtomicBoolean showUsage = new AtomicBoolean(false);

        if (args.length > 0 && args[0].equalsIgnoreCase("--explanation")) {
            new HelpExplanationSender().sendHelp(sender);
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        for (String argument : args) {
            if (argument.matches("--depth=.+")) {
                try {
                    depth.set(Integer.parseInt(argument.replace("--depth=", "")));
                } catch (NumberFormatException ignored) {

                }
            }
            else if (argument.matches("--showUsage=.+")) {
                showUsage.set(Boolean.parseBoolean(argument.replace("--showUsage=", "")));
            }
        }

        CommandNode node = commandTree.getRoot();

        // use the targeted node, if any
        {
            CommandFindResult findResult = commandTree.find(sender, args);
            if (findResult.wasFound()) {
                node = findResult.getCommandNode();
            }
        }

        List<PagerFilterable> filterable = getCommandFilterable(getMessageProvider(), node, commandTree, showUsage
                .get(), depth
                .get(), 0);

        Page page = Pager.getPageFromFilterable(options, filterable);

        page.send(sender, getMessageProvider());
        if (sender instanceof Player) {
            // @formatter:off
            JsonMessage
                    .create("                    ")
                    .next()
                    .text("Help explanation")
                    .color(ChatColor.DARK_AQUA)
                    .bold(true)
                    .hover()
                        .displayText(
                            JsonMessage.create("Shows the arguments for the help command.\n")
                                    .bold(true)
                                    .color(ChatColor.GREEN)
                                    .next("Just add them behind the help command, separated by spaces")
                                    .bold(true)
                                    .color(ChatColor.GREEN)
                                    .build()
                        )
                        .build()
                    .click()
                        .runCommand("/"
                                + getHelpCommandString(commandTree.getRoot(), new LinkedList<>())
                                + " --explanation")
                        .build()
                    .build()
                    .send((Player) sender);
            // @formatter:on
        }

        return CommandResult.SUCCESSFULLY_INVOKED;
    }

    private String getHelpCommandString(CommandNode node, Queue<String> current) {
        if (node == this) {
            return String.join(" ", current);
        }
        for (CommandNode commandNode : node.getChildren()) {
            Queue<String> queue = new LinkedList<>(current);
            queue.add(commandNode.getKeyword());
            String helpCommandString = getHelpCommandString(commandNode, queue);
            if (helpCommandString != null) {
                return helpCommandString;
            }
        }

        return null;
    }

    /**
     * Sends help for one command
     *
     * @param language The language to use for the key translation
     * @param node The CommandNode to start with
     * @param withUsage If true, the usage will be shown
     * @param maxDepth The maximum depth. Index based. 0 {@code ==>} Just this
     * command,
     * 1 {@code ==>} Command and children
     * @param counter The current counter. Just supply 0. Used for recursion.
     */
    private static List<PagerFilterable> getCommandFilterable(MessageProvider language, CommandNode node, CommandTree
            tree,
                                                              boolean withUsage, int maxDepth,
                                                              int counter) {
        List<PagerFilterable> list = new ArrayList<>();

        if (!tree.isRoot(node)) {
            PagerFilterable filterable = new CommandFilterable(node, withUsage, node.getChildren().size(),
                    language, counter);
            list.add(filterable);
        }
        else {
            counter--;
        }

        if (counter >= maxDepth) {
            return list;
        }

        for (CommandNode commandNode : node.getChildren()) {
            list.addAll(getCommandFilterable(language, commandNode, tree, withUsage, maxDepth, counter + 1));
        }

        return list;
    }

    private static class CommandFilterable implements PagerFilterable {

        private CommandNode node;
        private boolean showUsage;
        private String childrenAmount;
        private MessageProvider language;
        private int depth;

        private List<String> allLines;

        CommandFilterable(CommandNode node, boolean showUsage, int childrenAmount,
                          MessageProvider language, int depth) {
            this.node = node;
            this.showUsage = showUsage;
            this.childrenAmount = childrenAmount == 0 ? "" : Integer.toString(childrenAmount);
            this.language = language;
            this.depth = depth;

            calculateAllLines();
        }

        @Override
        public boolean accepts(Options options) {
            // match against what is shown
            for (String line : allLines) {
                if (options.matchesPattern(strip(line))) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param coloredString The String to strip the colors from
         *
         * @return The uncolored String
         */
        private static String strip(String coloredString) {
            return ChatColor.stripColor(coloredString);
        }

        @SuppressWarnings("StringConcatenationInLoop")
        private void calculateAllLines() {
            String finalString;
            {
                if (showUsage) {
                    String key = "command.help.format.with.usage";
                    finalString = language.translateOrDefault(key,
                            "&3{0}&9: &7{1} &7<&6{2}&7><newline>  &cUsage: {3}",
                            node.getName(), node.getDescription(), childrenAmount, node.getUsage());
                }
                else {
                    String key = "command.help.format.without.usage";
                    finalString = language.translateOrDefault(key,
                            "&3{0}&9: &7{1} &7<&6{2}&7>",
                            node.getName(), node.getDescription(), childrenAmount, node.getUsage());
                }
                finalString = colorize(finalString);
            }

            List<String> list = new ArrayList<>();

            for (String s : finalString.split("<newline>")) {
                if (depth == 0) {
                    s = colorize(language.translateOrDefault("command.help.top.level.prefix", "")) + s;
                }
                else {
                    s = colorize(language.translateOrDefault("command.help.sub.level.prefix", "")) + s;
                }
                s = TextUtils.repeat(language.translateOrDefault("command.help.padding.char", "  "), depth) + s;

                if (!s.isEmpty()) {
                    list.add(s);
                }
            }

            allLines = list;
        }

        @Override
        public
        @Nonnull
        List<String> getAllLines() {
            return allLines;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CommandFilterable)) {
                return false;
            }
            CommandFilterable that = (CommandFilterable) o;
            return Objects.equals(node, that.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node);
        }

        @Override
        public String toString() {
            return "CommandFilterable{" +
                    "node=" + node.getName() +
                    ", showUsage=" + showUsage +
                    ", childrenAmount='" + childrenAmount + '\'' +
                    ", depth=" + depth +
                    ", allLines=" + getAllLines() +
                    '}';
        }
    }

    private class HelpExplanationSender {

        private final String LANG_KEY = "command.help.explanation";

        /**
         * Sends the explanation to a {@link CommandSender}
         *
         * @param commandSender The {@link CommandSender} to send it to
         */
        void sendHelp(CommandSender commandSender) {
            commandSender.sendMessage(getMessage().toArray(new String[0]));
        }

        Collection<String> getMessage() {
            MessageProvider lang = getMessageProvider();
            if (lang.hasKey(LANG_KEY)) {
                Object object = lang.translateObject(LANG_KEY);
                if (object.getClass().isArray()) {
                    Object[] array = (Object[]) object;
                    return Arrays.stream(array).map(Objects::toString).collect(Collectors.toList());
                }
                else if (object instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) object;
                    return iteratorToString(iterable.iterator());
                }
                else if (object instanceof Iterator) {
                    return iteratorToString((Iterator<?>) object);
                }
            }

            //"--depth=", "--page=", "--entriesPerPage=", "--showUsage=",
            //"--search=", "--regex=", "--explanation
            List<String> defaultMessage = new ArrayList<>();
            defaultMessage.add("\n&a&l+&8&m-----------------&8 &a&lArguments &8&m------------------&a&l+\n ");
            addExplanation(
                    "--page=<number>",
                    "Selects a help page",
                    defaultMessage
            );
            addExplanation(
                    "--showUsage=<true|false>",
                    "Whether to show the command usage",
                    defaultMessage
            );
            addExplanation(
                    "--entriesPerPage=<number>",
                    "The entries to show on one page",
                    defaultMessage
            );
            addExplanation(
                    "--search=<query>",
                    "Filters the results. Can not contain spaces",
                    defaultMessage
            );
            addExplanation(
                    "--regex=<query>",
                    "Filters the results using a regex. Can not contain spaces",
                    defaultMessage
            );
            addExplanation(
                    "--depth=<number>",
                    "The amount of child-levels to show.",
                    defaultMessage
            );
            defaultMessage.add("\n&a&l+&8&m------------------------------------------&a&l+\n ");
            return ListUtils.colorList(defaultMessage);
        }

        private void addExplanation(String option, String explanation, List<String> targetList) {
            targetList.add(" &3" + option + "&9:");
            targetList.add(" &7  " + explanation);
        }

        private Collection<String> iteratorToString(Iterator<?> iterator) {
            Collection<String> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Object o = iterator.next();
                list.add(Objects.toString(o));
            }
            return list;
        }

    }

    /**
     * Parses the Builder options
     */
    private static class OptionsParser {
        private Map<Pattern, BiConsumer<String, Options.Builder>> optionsParserMap = new HashMap<>();

        {
            optionsParserMap.put(Pattern.compile("--page=.+", CASE_INSENSITIVE), (s, builder) -> {
                String page = s.replace("--page=", "");
                Integer integer = toInt(page);
                if (integer != null) {
                    builder.setPageIndex(integer);
                }
            });

            optionsParserMap.put(Pattern.compile("--entriesPerPage=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--entriesPerPage=", "");
                Integer integer = toInt(entries);
                if (integer != null) {
                    builder.setEntriesPerPage(integer);
                }
            });

            optionsParserMap.put(Pattern.compile("--search=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--search=", "");
                builder.setSearchModes(SearchMode.CONTAINS_IGNORE_CASE);
                builder.setSearchPattern(entries);
            });

            optionsParserMap.put(Pattern.compile("--regex=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--regex=", "");
                builder.setSearchModes(SearchMode.REGEX_FIND_CASE_INSENSITIVE);
                builder.setSearchPattern(entries);
            });

            // special options (not shown, but there :P)

            optionsParserMap.put(Pattern.compile("--contains=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--contains=", "");
                builder.setSearchModes(SearchMode.CONTAINS);
                builder.setSearchPattern(entries);
            });

            optionsParserMap.put(Pattern.compile("--regexFind=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--regexFind=", "");
                builder.setSearchModes(SearchMode.REGEX_FIND);
                builder.setSearchPattern(entries);
            });

            optionsParserMap.put(Pattern.compile("--regexMatch=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--regexMatch=", "");
                builder.setSearchModes(SearchMode.REGEX_MATCHES);
                builder.setSearchPattern(entries);
            });

            optionsParserMap.put(Pattern.compile("--regexMatchCI=.+", CASE_INSENSITIVE), (s, builder) -> {
                String entries = s.replace("--regexMatchCI=", "");
                builder.setSearchModes(SearchMode.REGEX_MATCHES_CASE_INSENSITIVE);
                builder.setSearchPattern(entries);
            });
        }

        /**
         * Parses the options
         *
         * @param args The Arguments to parse
         *
         * @return The parsed options
         */
        Options parse(String[] args) {
            Options.Builder builder = Options.builder();
            for (String argument : args) {
                for (Entry<Pattern, BiConsumer<String, Options.Builder>> entry : optionsParserMap.entrySet()) {
                    Matcher matcher = entry.getKey().matcher(argument);
                    if (matcher.find()) {
                        entry.getValue().accept(argument, builder);
                    }
                }
            }
            return builder.build();
        }

        /**
         * @param string The String to convert
         *
         * @return The converted Integer or null if not parsable
         */
        private static Integer toInt(String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
