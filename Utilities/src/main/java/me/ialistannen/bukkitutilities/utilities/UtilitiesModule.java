package me.ialistannen.bukkitutilities.utilities;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * Contains common utility methods and classes
 */
public class UtilitiesModule extends AbstractModule {

    /**
     * Creates a new {@link UtilitiesModule}
     */
    public UtilitiesModule() {
        super(getModulePropertiesFromJar(UtilitiesModule.class));
    }
}
