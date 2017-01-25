package me.ialistannen.bukkitutilities.packet;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * Adds some basic packet support.
 */
public class PacketModule extends AbstractModule {

    /**
     * Creates a new PacketModule
     */
    public PacketModule() {
        super(getModulePropertiesFromJar(PacketModule.class));
    }
}
