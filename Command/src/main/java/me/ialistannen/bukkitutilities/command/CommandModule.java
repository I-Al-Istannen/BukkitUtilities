package me.ialistannen.bukkitutilities.command;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * Allows you do specify commands
 */
public class CommandModule extends AbstractModule {

    /**
     * Constructs a new {@link CommandModule}
     */
    public CommandModule() {
        super(getModulePropertiesFromJar(CommandModule.class));
    }
}
