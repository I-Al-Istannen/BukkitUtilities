package me.ialistannen.bukkitutilities.config.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.ialistannen.bukkitutilities.config.ConfigSerializable;
import me.ialistannen.bukkitutilities.config.SerializationManager;


/**
 * A {@link DataManager} that saves all data in one file, organized by sections
 * that the key as their name
 */
public class DataFileManager <K, V extends ConfigSerializable> extends DataManager<K, V> {

    /**
     * Creates a new {@link DataFileManager} that saves and loads data
     * class of the
     * type specified, and stores them in the given map.
     *
     * @param path The path to the data file
     * @param keyClass The class for the key.
     * {@link SerializationManager#isSerializableToString(Class)}
     * must return true when given this.
     * @param dataClass The data class that this {@link DataManager} handles
     * @param map The map to store the data in
     */
    @SuppressWarnings("WeakerAccess")
    public DataFileManager(Path path, Class<K> keyClass, Class<V> dataClass, Map<K, V> map) {
        super(path, keyClass, dataClass, map);
    }

    /**
     * Creates a new {@link DataFileManager} that uses a
     * {@link HashMap}
     *
     * @param path The path to the data file
     * @param keyClass The class for the key.
     * {@link SerializationManager#isSerializableToString(Class)}
     * must return true when given this.
     * @param dataClass The data class that this {@link DataManager} handles
     *
     * @see #DataFileManager(Path, Class, Class, Map)
     */
    @SuppressWarnings("WeakerAccess")
    public DataFileManager(Path path, Class<K> keyClass, Class<V> dataClass) {
        this(path, keyClass, dataClass, new HashMap<>());
    }

    /**
     * Creates a new {@link DataManager}
     *
     * @param plugin The plugin to get the Data folder from
     * @param path The path to the data file
     * @param keyClass The class for the key.
     * {@link SerializationManager#isSerializableToString(Class)}
     * must return true when given this.
     * @param dataClass The data class that this {@link DataManager} handles
     *
     * @see #DataFileManager(Path, Class, Class)
     */
    @SuppressWarnings("unused")
    public DataFileManager(Plugin plugin, String path, Class<K> keyClass, Class<V> dataClass) {
        this(plugin.getDataFolder().toPath().resolve(normalizePathName(path)), keyClass, dataClass);
    }

    @Override
    public boolean isValidPath(Path path) {
        return Files.notExists(path) || Files.isRegularFile(path);
    }

    @Override
    public void save() {
        YamlConfiguration configuration = new YamlConfiguration();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            Object serializedKey = SerializationManager.serializeOneLevel(entry.getKey());
            if (!(serializedKey instanceof String)) {
                LOGGER.log(Level.WARNING, "Fascinating."
                        + " A class somehow broke the promise of SerializationManager to serialize to a String!"
                        + " Class: " + entry.getKey().getClass()
                        + " Value: " + entry.getKey());
                continue;
            }

            Map<String, Object> serializedValue = SerializationManager.serialize(entry.getValue());

            configuration.createSection((String) serializedKey, serializedValue);
        }

        try {
            configuration.save(getPath().toFile());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "DataFileManager failed to save it's data to the disk! "
                    + "This is most likely not the fault of BukkitUtilities.", e);
        }
    }

    @Override
    public void load() {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getPath().toFile());

        clear();

        for (String configKey : configuration.getKeys(false)) {
            V value = SerializationManager.deserialize(
                    getDataClass(),
                    configuration.getConfigurationSection(configKey)
            );
            @SuppressWarnings("unchecked")
            K key = (K) SerializationManager.deserializeOneLevel(configKey, getKeyClass());

            put(key, value);
        }
    }
}
