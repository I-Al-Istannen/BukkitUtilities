package me.ialistannen.bukkitutilities.gui.components.simple;

import java.util.function.Function;

import org.bukkit.Material;

import me.ialistannen.bukkitutilities.utilities.item.DisplayColor;
import me.ialistannen.bukkitutilities.utilities.item.ItemFactory;

/**
 * The display type
 */
public enum StandardDisplayTypes implements DisplayType {

    /**
     * A "flat" display. Uses {@link Material#STAINED_GLASS_PANE}
     */
    FLAT(color -> color.getItemFactory(Material.STAINED_GLASS_PANE)),
    /**
     * A "cubic" display. Uses {@link Material#STAINED_CLAY}
     */
    @SuppressWarnings("unused")
    CUBE(color -> color.getItemFactory(Material.STAINED_CLAY)),
    /**
     * Uses {@link Material#WOOL}
     */
    @SuppressWarnings("unused")
    WOOL(color -> color.getItemFactory(Material.WOOL)),
    /**
     * Uses {@link Material#CARPET}
     */
    @SuppressWarnings("unused")
    CARPET(color -> color.getItemFactory(Material.CARPET)),
    /**
     * Uses {@link Material#INK_SACK} (which is dye)
     */
    @SuppressWarnings("unused")
    DYE(color -> {
        short correctedDurability = (short) (15 - color.getDataValue());
        return color.getItemFactory(Material.INK_SACK).setDurability(correctedDurability);
    });

    private Function<DisplayColor, ItemFactory> colorFunction;

    StandardDisplayTypes(Function<DisplayColor, ItemFactory> colorFunction) {
        this.colorFunction = colorFunction;
    }

    /**
     * Colors the item
     *
     * @param color The Color to apply
     *
     * @return The colored item
     */
    @Override
    public ItemFactory getColouredItem(DisplayColor color) {
        return colorFunction.apply(color);
    }
}
