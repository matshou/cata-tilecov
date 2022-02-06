package io.matshou.cata.tilecov.config;

public class IllegalConfigPropertyException extends ConfigPropertyException {

    IllegalConfigPropertyException(Config.Entry property, String reason) {
        super(String.format("%s: %s", property.name, reason));
    }
}
