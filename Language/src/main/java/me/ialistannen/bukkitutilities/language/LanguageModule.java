package me.ialistannen.bukkitutilities.language;

import me.ialistannen.bukkitutilities.modulesystem.AbstractModule;

/**
 * Translations!
 */
public class LanguageModule extends AbstractModule {

    /**
     * Creates the language module
     */
    public LanguageModule() {
        super(getModulePropertiesFromJar(LanguageModule.class));
    }
}
