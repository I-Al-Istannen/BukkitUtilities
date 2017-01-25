package me.ialistannen.bukkitutilities.updater;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * An updater for your plugins
 */
public class UpdaterModule extends AbstractModule {

    public UpdaterModule() {
        super(getModulePropertiesFromJar(UpdaterModule.class));
    }
}
