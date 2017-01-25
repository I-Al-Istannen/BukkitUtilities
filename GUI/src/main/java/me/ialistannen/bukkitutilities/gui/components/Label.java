package me.ialistannen.bukkitutilities.gui.components;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.ialistannen.bukkitutilities.gui.ClickEvent;
import me.ialistannen.bukkitutilities.gui.base.AbstractComponent;
import me.ialistannen.bukkitutilities.gui.util.Dimension;

/**
 * A Label. Like a button, but does nothing on click
 */
public class Label extends AbstractComponent {

    private ItemStack itemStack;

    /**
     * Constructs a Label
     *
     * @param itemStack The ItemStack to use
     * @param size The size of this component
     *
     * @throws NullPointerException if any parameter is null
     */
    @SuppressWarnings("WeakerAccess")
    public Label(ItemStack itemStack, Dimension size) {
        super(size);
        Objects.requireNonNull(itemStack);

        this.itemStack = itemStack.clone();
    }

    /**
     * Constructs a label with size {@link Dimension#ONE}
     *
     * @param itemStack The ItemStack to use
     */
    @SuppressWarnings("unused")
    public Label(ItemStack itemStack) {
        this(itemStack, Dimension.ONE);
    }

    /**
     * Sets the item
     *
     * @param itemStack The new itemstack
     */
    @SuppressWarnings("unused")
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    /**
     * Returns a clone of the {@link ItemStack} of this button
     *
     * @return The {@link ItemStack}
     */
    @SuppressWarnings("WeakerAccess")
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        clickEvent.setCancelled(true);
    }

    @Override
    public void render(Inventory inventory, Player player, int xOffset, int yOffset) {
        iterateOver2DRange(0, getSize().getWidth(), 0, getSize().getHeight(), (x, y) -> {
            int slot = gridToSlot(x + xOffset, y + yOffset);
            if (slot < 0 || slot >= inventory.getSize()) {
                // can't happen *normally*
                // @formatter:off
                System.err.println("Label: An item was placed outside the inventory size."
                          + " Size: " + inventory.getSize()
                          + " Slot: " + slot);
                // @formatter:on
            }
            else {
                inventory.setItem(slot, itemStack);
            }
        });
    }

    /**
     * The returned Label will be logically equal to this, but equals will
     * return false. This is because of a hidden ID assigned, to distinguish
     * instances.
     *
     * @return A clone of this label
     */
    @Override
    public Label deepClone() {
        try {
            return new Label(itemStack.clone(), getSize().clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
