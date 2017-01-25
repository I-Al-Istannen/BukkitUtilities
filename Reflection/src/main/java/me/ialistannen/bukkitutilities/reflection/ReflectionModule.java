package me.ialistannen.bukkitutilities.reflection;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * The reflection module
 */
public class ReflectionModule extends AbstractModule {

    /**
     * Creates the reflection module
     */
    public ReflectionModule() {
        super(getModulePropertiesFromJar(ReflectionModule.class));
    }
}
