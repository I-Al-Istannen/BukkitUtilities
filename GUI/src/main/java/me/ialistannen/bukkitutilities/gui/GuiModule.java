package me.ialistannen.bukkitutilities.gui;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * A module to allow working with guis
 */
public class GuiModule extends AbstractModule {

    /**
     * Creates the Gui module
     */
    public GuiModule() {
        super(getModulePropertiesFromJar(GuiModule.class));
    }
}
