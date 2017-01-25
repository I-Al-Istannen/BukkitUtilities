package me.ialistannen.bukkitutilities.config.handlers;

import java.util.UUID;

import me.ialistannen.bukkitutilities.config.SerializationManager;
import me.ialistannen.bukkitutilities.config.SimpleSerializationProxy;


/**
 * Adds the ability for {@link SerializationManager} to serialize and
 * deserialize objects of type {@link UUID}
 *
 * @author Rayzr
 */
public class UUIDSerializer implements SimpleSerializationProxy<UUID> {

    @Override
    public Object serializeSimple(UUID object) {
        return object.toString();
    }

    @Override
    public UUID deserializeSimple(Object data) {
        return UUID.fromString(data.toString());
    }

}
