package me.ialistannen.bukkitutilities.nbt;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;
import me.ialistannen.bukkitutilities.modulesystem.Module;

/**
 * The NBT {@link Module}
 */
public class NbtModule extends AbstractModule {

    /**
     * Creates the NBT module
     */
    public NbtModule() {
        super(getModulePropertiesFromJar(NbtModule.class));
    }
}
