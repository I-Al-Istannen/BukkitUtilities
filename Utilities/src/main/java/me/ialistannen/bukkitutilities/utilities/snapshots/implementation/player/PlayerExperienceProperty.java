package me.ialistannen.bukkitutilities.utilities.snapshots.implementation.player;

import org.bukkit.entity.Player;

import me.ialistannen.bukkitutilities.utilities.snapshots.SnapshotProperty;


/**
 * The property for the player Level
 */
public class PlayerExperienceProperty extends SnapshotProperty<Player> {

    private int totalXp = -1;

    @Override
    public void restoreFor(Player target) {
        throwUninitializedIfTrue(totalXp < 0);

        target.setTotalExperience(totalXp);
    }

    @Override
    public SnapshotProperty<Player> update(Player target) {
        totalXp = target.getTotalExperience();
        return this;
    }

    @Override
    public SnapshotProperty<Player> createForTarget(Player target) {
        return new PlayerExperienceProperty().update(target);
    }
}
