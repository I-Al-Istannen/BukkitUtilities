package me.ialistannen.bukkitutilities.utilities.text;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import me.ialistannen.bukkitutilities.nbt.NBTWrappers.NBTTagCompound;
import me.ialistannen.bukkitutilities.reflection.ReflectionUtil;
import me.ialistannen.bukkitutilities.reflection.ReflectionUtil.MethodPredicate;

import static me.ialistannen.bukkitutilities.reflection.ReflectionUtil.NameSpace.NMS;
import static me.ialistannen.bukkitutilities.reflection.ReflectionUtil.NameSpace.OBC;
import static me.ialistannen.bukkitutilities.utilities.text.TextUtils.enumFormat;

/**
 * A class to deal with JSON Messages
 */
public class JsonMessage {

    private static Class<?> CRAFT_ITEM_STACK = ReflectionUtil.getClass(OBC, "inventory.CraftItemStack")
            .orElseThrow(() -> new RuntimeException("Could not find the 'CraftItemStack' class"));
    private static Method AS_NMS_COPY = ReflectionUtil.getMethod(
            CRAFT_ITEM_STACK,
            new MethodPredicate().withName("asNMSCopy")
    ).getValueOrThrow("Could not find the 'asNMSCopy' method");
    private static Class<?> NMS_ITEM_STACK = ReflectionUtil.getClass(NMS, "ItemStack")
            .orElseThrow(() -> new RuntimeException("Could not find the nms 'ItemStack' class"));
    private static Method SAVE = ReflectionUtil.getMethod(NMS_ITEM_STACK, new MethodPredicate().withName("save"))
            .getValueOrThrow("Could not find the 'save' method");

    private static Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(HoverEvent.class, new HoverEventSerializer())
            .registerTypeAdapter(ChatColor.class, new EnumSerializer<>(tEnum -> tEnum.name().toLowerCase()))
            .registerTypeAdapter(HoverAction.class, new EnumSerializer<>())
            .registerTypeAdapter(ClickAction.class, new EnumSerializer<>())
            .registerTypeAdapterFactory(
                    new AnonymousClassesFactory<>(
                            ChatColor.class,
                            (gson, typeToken) -> new EnumSerializer<>(tEnum -> tEnum.name().toLowerCase())
                    )
            )
            .create();

    /**
     * @return A new empty {@link MessageBuilder}
     */
    @SuppressWarnings("unused")
    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    /**
     * @param initial The initial text
     *
     * @return A new empty {@link MessageBuilder}
     */
    @SuppressWarnings("unused")
    public static MessageBuilder create(String initial) {
        return new MessageBuilder().text(initial);
    }

    /**
     * A finished JSON message
     */
    public static class BuiltJsonMessage {
        private MessageBuilder builder;

        BuiltJsonMessage(MessageBuilder builder) {
            this.builder = builder;
        }

        /**
         * @return The JSON String
         */
        @SuppressWarnings("unused")
        public String getJson() {
            String s = GSON.toJson(builder.parts);
            // **** You gson. I do not care about script tags!
            s = s.replace("\\\\/", "/");
            // **** you minecraft. Why do you use some invalid json monstrosity?
            s = StringEscapeUtils.unescapeJson(s);
            return s;
        }

        /**
         * Sends the message to all the specified players
         *
         * @param players The Players to send it to
         */
        @SuppressWarnings("unused")
        public void send(Player... players) {
            String json = getJson();
            for (Player player : players) {
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "tellraw " + player.getName() + " " + json
                );
            }
        }
    }

    /**
     * A Builder for Json messages
     */
    public static class MessageBuilder {
        private List<MessagePart> parts = new ArrayList<>();
        private MessagePart current = new MessagePart();

        /**
         * Adds an empty text part
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder addEmpty() {
            MessagePart empty = new MessagePart();
            empty.text = "";
            parts.add(empty);
            return this;
        }

        /**
         * Adds this component and switches to the next
         *
         * @return This builder
         */
        @SuppressWarnings("WeakerAccess")
        public MessageBuilder next() {
            parts.add(current);
            current = new MessagePart();
            return this;
        }

        /**
         * Adds this component and switches to the next. Then adds the given text
         *
         * @param text The initial test
         *
         * @return This builder
         *
         * @see #next()
         */
        @SuppressWarnings("unused")
        public MessageBuilder next(String text) {
            return next().text(text);
        }

        /**
         * Adds a message inheriting the formatting of the current one
         *
         * @param extra The extra message
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder extra(BuiltJsonMessage extra) {
            current.addExtras(extra.builder.parts);
            return this;
        }

        /**
         * Adds the given text
         *
         * @param text The text to add
         *
         * @return This builder
         */
        @SuppressWarnings("WeakerAccess")
        public MessageBuilder text(String text) {
            current.setText(text);
            return this;
        }

        /**
         * Adds a selector (like {@code "@p"}). It will be evaluated and replaced with the result
         *
         * @param selector The selector to add.
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder selector(String selector) {
            current.setSelector(selector);
            return this;
        }

        /**
         * Adds a value that will be replaced by the given scoreboard objective
         *
         * @param score The score to add
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder score(Score score) {
            current.setScore(score);
            return this;
        }

        /**
         * Sets the color of the part
         *
         * @param chatColor The color of the message
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder color(ChatColor chatColor) {
            Validate.isTrue(chatColor.isColor(), "chatColor must be a color, not a format!");
            current.setColor(chatColor);
            return this;
        }

        /**
         * @param underline Whether it should be underlined
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder underlined(boolean underline) {
            current.setUnderlined(underline);
            return this;
        }

        /**
         * @param bold Whether it should be bold
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder bold(boolean bold) {
            current.setBold(bold);
            return this;
        }

        /**
         * @param obfuscated Whether it should be obfuscated (or magic)
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder obfuscated(boolean obfuscated) {
            current.setObfuscated(obfuscated);
            return this;
        }

        /**
         * @param italic Whether it will be italic
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder italic(boolean italic) {
            current.setItalic(italic);
            return this;
        }

        /**
         * Specifies the text to insert upon clicking with SHIFT+LEFT
         *
         * @param insertion The text to insert
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder insertion(String insertion) {
            current.setInsertion(insertion);
            return this;
        }

        /**
         * Translates a minecraft message (like "multiplayer.player.joined", which takes ONE argument)
         *
         * @param translate The key to the message to replace
         * @param with The replacements
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public MessageBuilder translate(String translate, String... with) {
            current.setTranslate(translate);
            current.setWith(with);
            return this;
        }

        /**
         * @return A builder for the Hover event
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder hover() {
            return new HoverEventBuilder(this);
        }

        void setHover(HoverEvent hoverEvent) {
            current.setHoverEvent(hoverEvent);
        }

        /**
         * @return A builder for the Click event
         */
        @SuppressWarnings("unused")
        public ClickEventBuilder click() {
            return new ClickEventBuilder(this);
        }

        void setClick(ClickEvent clickEvent) {
            current.setClickEvent(clickEvent);
        }

        /**
         * Builds this message
         *
         * @return A built message
         */
        public BuiltJsonMessage build() {
            parts.add(current);
            return new BuiltJsonMessage(this);
        }
    }

    /**
     * A score
     */
    public static class Score {
        @SuppressWarnings("unused")
        private String name, objective, value;

        Score(String name, String objective, String value) {
            this.name = name;
            this.objective = objective;
            this.value = value;
        }

        /**
         * Creates a score for a player and an objective
         *
         * @param name The name of the entity. May be a selector
         * @param objective The objective to use
         *
         * @return The Score
         */
        public static Score of(String name, String objective) {
            return new Score(name, objective, null);
        }

        /**
         * Creates a score for a player and an objective
         *
         * @param name The name of the entity. May be a selector
         * @param objective The objective to use
         * @param value The value of the score
         *
         * @return The Score
         */
        public static Score of(String name, String objective, int value) {
            return new Score(name, objective, Integer.toString(value));
        }

        /**
         * Creates a score for a player and an objective
         *
         * @param name The name of the entity. May be a selector
         * @param objective The objective to use
         * @param value The value of the score
         *
         * @return The Score
         */
        public static Score of(String name, String objective, String value) {
            return new Score(name, objective, value);
        }
    }

    /**
     * A Builder for a hover event
     */
    public static class HoverEventBuilder {
        private MessageBuilder source;
        private HoverEvent event = new HoverEvent();

        HoverEventBuilder(MessageBuilder source) {
            this.source = source;
        }

        /**
         * @param text The text to display
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayText(String text) {
            event.displayText(text);
            return this;
        }

        /**
         * @param message The text to display
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayText(BuiltJsonMessage message) {
            event.displayText(message.builder.parts);
            return this;
        }

        /**
         * Displays an Entity popup
         *
         * @param name The name of the entity
         * @param type The type of the entity
         * @param id The id of the entity
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayEntity(String name, String type, String id) {
            event.displayEntity(name, type, id);
            return this;
        }

        /**
         * Displays an Achievement in a popup
         *
         * @param achievement The {@link Achievement} to display
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayAchievement(Achievement achievement) {
            event.displayAchievement(achievement);
            return this;
        }

        /**
         * Displays a statistic in a popup
         *
         * @param statistic The {@link Statistic} to display
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayStatistic(Statistic statistic) {
            event.displayStatistic(statistic);
            return this;
        }

        /**
         * Displays an item in a popup
         *
         * @param itemStack The item to display
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public HoverEventBuilder displayItem(ItemStack itemStack) {
            event.displayItem(itemStack);
            return this;
        }

        /**
         * Builds this event
         *
         * @return The original {@link MessageBuilder}
         */
        public MessageBuilder build() {
            source.setHover(event);
            return source;
        }
    }

    /**
     * A Builder for click events
     */
    public static class ClickEventBuilder {
        private MessageBuilder source;
        private ClickEvent clickEvent;

        ClickEventBuilder(MessageBuilder source) {
            this.source = source;
            clickEvent = new ClickEvent();
        }

        /**
         * Opens an URL on click
         *
         * @param url The url to open
         *
         * @return This Builder
         */
        @SuppressWarnings("unused")
        public ClickEventBuilder openUrl(String url) {
            clickEvent.openUrl(url);
            return this;
        }

        /**
         * Suggests a command
         *
         * @param command The command to suggest
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public ClickEventBuilder suggestCommand(String command) {
            clickEvent.suggestCommand(command);
            return this;
        }

        /**
         * Runs a command when clicked
         *
         * @param command The command to run. Needs to start with a slash, or the user will chat the message
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public ClickEventBuilder runCommand(String command) {
            clickEvent.runCommand(command);
            return this;
        }

        /**
         * Switches the page on click. Only for books.
         *
         * @param newPage The new page
         *
         * @return This builder
         */
        @SuppressWarnings("unused")
        public ClickEventBuilder changePage(int newPage) {
            clickEvent.changePage(newPage);
            return this;
        }

        /**
         * Builds this click event
         *
         * @return The original builder
         */
        public MessageBuilder build() {
            source.setClick(clickEvent);
            return source;
        }
    }

    /**
     * The click Action
     */
    private enum ClickAction {
        RUN_COMMAND("run_command"),
        SUGGEST_COMMAND("suggest_command"),
        OPEN_URL("open_url"),
        CHANGE_PAGE("change_page");

        private String name;

        ClickAction(String name) {
            this.name = name;
        }

        /**
         * @return The minecraft name of it
         */
        public String toString() {
            return name;
        }
    }

    /**
     * The action for clicking on an Hover event
     */
    private enum HoverAction {
        SHOW_TEXT("show_text"),
        SHOW_ITEM("show_item"),
        SHOW_ACHIEVEMENT("show_achievement"),
        SHOW_ENTITY("show_entity");

        private String name;

        HoverAction(String name) {
            this.name = name;
        }

        /**
         * @return The minecraft name of it
         */
        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("unused")
    private static class MessagePart {
        private String text;
        private String translate;
        private List<String> with;
        private String selector;
        private Score score;
        private ChatColor color;
        // use the object to inherit correctly (if it is null, it will not be appended by GSON)
        private Boolean bold, italic, underlined, obfuscated;
        private HoverEvent hoverEvent;
        private ClickEvent clickEvent;
        private String insertion;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private List<MessagePart> extra;

        void setText(String text) {
            this.text = text;
        }

        void setTranslate(String translate) {
            this.translate = translate;
        }

        void setWith(List<String> with) {
            this.with = with;
        }

        void setWith(String... more) {
            List<String> withList = new ArrayList<>(more.length);
            Collections.addAll(withList, more);
            setWith(withList);
        }

        void setSelector(String selector) {
            this.selector = selector;
        }

        void setScore(Score score) {
            this.score = score;
        }

        void setColor(ChatColor color) {
            this.color = color;
        }

        void setBold(Boolean bold) {
            this.bold = bold;
        }

        void setItalic(Boolean italic) {
            this.italic = italic;
        }

        void setUnderlined(Boolean underlined) {
            this.underlined = underlined;
        }

        void setObfuscated(Boolean obfuscated) {
            this.obfuscated = obfuscated;
        }

        void setHoverEvent(HoverEvent hoverEvent) {
            this.hoverEvent = hoverEvent;
        }

        void setClickEvent(ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

        void setInsertion(String insertion) {
            this.insertion = insertion;
        }

        void addExtras(Collection<MessagePart> part) {
            extra.addAll(part);
        }
    }

    private static class HoverEvent {
        private HoverAction action;
        private MessagePart[] value;
        private String itemValue;

        void displayItem(ItemStack item) {
            action = HoverAction.SHOW_ITEM;

            Object nmsItem = ReflectionUtil.invokeMethod(AS_NMS_COPY, null, item)
                    .getValueOrThrow("Could not invoke 'asNMSCopy' method");
            Object savedTag = ReflectionUtil.invokeMethod(SAVE, nmsItem, new NBTTagCompound().toNBT())
                    .getValueOrThrow("Could not invoke 'save' method");

            String string = savedTag.toString();
            itemValue = StringEscapeUtils.escapeJson(string);
        }

        void displayText(String text) {
            action = HoverAction.SHOW_TEXT;
            MessagePart part = new MessagePart();
            part.text = text;
            value = new MessagePart[]{part};
        }

        void displayText(MessagePart[] text) {
            action = HoverAction.SHOW_TEXT;
            value = text;
        }

        void displayText(Collection<MessagePart> text) {
            displayText(text.toArray(new MessagePart[text.size()]));
        }

        void displayEntity(String name, String type, String id) {
            action = HoverAction.SHOW_ENTITY;

            String escapedName = StringEscapeUtils.escapeJson(name);

            String escapedType = StringEscapeUtils.escapeJson(type);
            if (escapedType.matches(".+\\s.+")) {
                escapedType = "\"" + escapedType + "\"";
            }

            String escapedId = StringEscapeUtils.escapeJson(id);
            MessagePart part = new MessagePart();
            part.text = "\"{name:" + escapedName + ",type:" + escapedType + ",id:" + escapedId + "}\"";
            value = new MessagePart[]{part};
        }

        void displayAchievement(Achievement achievement) {
            action = HoverAction.SHOW_ACHIEVEMENT;
            String replace = enumFormat(achievement.name(), true).replace(" ", "");
            replace = replace.substring(0, 1).toLowerCase() + replace.substring(1);
            MessagePart part = new MessagePart();
            part.text = "achievement." + replace;
            value = new MessagePart[]{part};
        }

        void displayStatistic(Statistic statistic) {
            action = HoverAction.SHOW_ACHIEVEMENT;
            String replace = enumFormat(statistic.name(), true).replace(" ", "");
            replace = replace.substring(0, 1).toLowerCase() + replace.substring(1);
            MessagePart part = new MessagePart();
            part.text = "stat." + replace;
            value = new MessagePart[]{part};
        }
    }

    private static class HoverEventSerializer implements JsonSerializer<HoverEvent> {
        /**
         * Gson invokes this call-back method during serialization when it encounters a field of the
         * specified type.
         * <p>
         * <p>In the implementation of this call-back method, you should consider invoking
         * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
         * non-trivial field of the {@code src} object. However, you should never invoke it on the
         * {@code src} object itself since that will cause an infinite loop (Gson will call your
         * call-back method again).</p>
         *
         * @param src the object that needs to be converted to Json.
         * @param typeOfSrc the actual type (fully genericized version) of the source object.
         * @param context The context
         *
         * @return a JsonElement corresponding to the specified object.
         */
        @Override
        public JsonElement serialize(HoverEvent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();
            serialized.add("action", context.serialize(src.action));
            if (src.itemValue == null) {
                serialized.add("value", context.serialize(src.value));
            }
            else {
                serialized.add("value", context.serialize(src.itemValue));
            }
            return serialized;
        }
    }

    private static class ClickEvent {
        @SuppressWarnings("unused")
        private ClickAction action;
        @SuppressWarnings("unused")
        private String value;

        void runCommand(String command) {
            if (command.length() > 256) {
                throw new IllegalArgumentException("The command must be <= 256 chars");
            }
            action = ClickAction.RUN_COMMAND;
            value = command;
        }

        void suggestCommand(String command) {
            action = ClickAction.SUGGEST_COMMAND;
            value = command;
        }

        void openUrl(String url) {
            action = ClickAction.OPEN_URL;
            value = StringEscapeUtils.escapeJson(url);
        }

        void changePage(int newPage) {
            value = Integer.toString(newPage);
            action = ClickAction.CHANGE_PAGE;
        }
    }

    private static class AnonymousClassesFactory <U> implements TypeAdapterFactory {

        private BiFunction<Gson, TypeToken<U>, TypeAdapter<U>> createToken;
        private Class<U> targetClass;

        AnonymousClassesFactory(Class<U> targetClass, BiFunction<Gson, TypeToken<U>, TypeAdapter<U>> createToken) {
            this.createToken = createToken;
            this.targetClass = targetClass;
        }

        /**
         * Returns a type adapter for {@code type}, or null if this factory doesn't
         * support {@code type}.
         *
         * @param gson The gson instance
         * @param type The type
         */
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().isAnonymousClass()) {
                return null;
            }
            if (type.getRawType().getSuperclass() != targetClass) {
                return null;
            }
            @SuppressWarnings("unchecked")
            TypeToken<U> typeToken = (TypeToken<U>) type;
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) createToken.apply(gson, typeToken);

            return result;
        }
    }

    private static class EnumSerializer <T extends Enum<T>> extends TypeAdapter<T> {

        private Function<Enum<T>, String> converter = Enum::toString;

        EnumSerializer() {
        }

        EnumSerializer(Function<Enum<T>, String> converter) {
            this.converter = converter;
        }

        /**
         * Writes one JSON value (an array, object, string, number, boolean or null)
         * for {@code value}.
         *
         * @param out The {@link JsonWriter}
         * @param value the Java object to write. May be null.
         */
        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(converter.apply(value));
        }

        /**
         * Reads one JSON value (an array, object, string, number, boolean or null)
         * and converts it to a Java object. Returns the converted object.
         *
         * @param in The {@link JsonReader}
         *
         * @return the converted Java object. May be null.
         */
        @Override
        public T read(JsonReader in) throws IOException {
            return null;
        }
    }
}