package me.ialistannen.bukkitutilities.utilities.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Player;

import me.ialistannen.bukkitutilities.config.ConfigSerializable;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.EntityHealthProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.EntityLocationProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.EntityPotionEffectProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.PlayerExperienceProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.PlayerFoodProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.PlayerGamemodeProperty;
import me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player.PlayerInventoryProperty;

/**
 * A snapshot
 *
 * @param <T> The type of the object which the snapshot is for
 */
public class Snapshot <T> implements ConfigSerializable {

    private List<SnapshotProperty<? super T>> snapshotProperties = new ArrayList<>();

    /**
     * For serializing
     */
    @SuppressWarnings("unused")
    private Snapshot() {
    }

    /**
     * Creates a Snapshot for the given Target and the given properties
     *
     * @param target The target to snapshot
     * @param properties The properties to snapshot
     */
    @SuppressWarnings("WeakerAccess")
    public Snapshot(T target, Collection<SnapshotProperty<? super T>> properties) {
        Objects.requireNonNull(target, "target cannot be null!");
        Objects.requireNonNull(properties, "properties cannot be null!");

        properties.stream()
                .map(property -> property.createForTarget(target))
                .sequential()
                .forEach(snapshotProperties::add);
    }

    /**
     * Creates a Snapshot for the given Target and the given properties
     *
     * @param target The target to snapshot
     * @param properties The properties to snapshot
     *
     * @see #Snapshot(Object, Collection)
     */
    @SuppressWarnings("WeakerAccess")
    @SafeVarargs
    public Snapshot(T target, SnapshotProperty<? super T>... properties) {
        this(target, Arrays.asList(properties));
    }

    /**
     * Restores this state for the given target
     *
     * @param target The target to restoreFor it to
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void restore(T target) {
        Objects.requireNonNull(target, "target cannot be null!");

        snapshotProperties.forEach(property -> property.restoreFor(target));
    }

    /**
     * Updates its inner state to the one of the given target
     *
     * @param target The target to update its state from
     */
    public void update(T target) {
        Objects.requireNonNull(target, "target cannot be null!");

        snapshotProperties.forEach(property -> property.update(target));
    }

    /**
     * Snapshots the current state of the player.
     * <p>
     * You can later restore it to an arbitrary Player using
     * {@link #restore(Object)}.
     * <p>
     * You can update it by using {@link #update(Object)}
     *
     * @param player The player to snapshot
     *
     * @return A snapshot for the player
     */
    @SuppressWarnings("unused")
    public static Snapshot<Player> ofPlayer(Player player) {
        return new Snapshot<>(player,
                new EntityHealthProperty(), new EntityLocationProperty(), new EntityPotionEffectProperty(),
                new PlayerExperienceProperty(), new PlayerFoodProperty(), new PlayerGamemodeProperty(), new
                PlayerInventoryProperty());
    }
}
