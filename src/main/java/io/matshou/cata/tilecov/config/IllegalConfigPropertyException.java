package io.matshou.cata.tilecov.config;

public class IllegalConfigPropertyException extends ConfigPropertyException {

    IllegalConfigPropertyException(String property, String reason) {
        super(String.format("%s: %s", property, reason));
    }

    IllegalConfigPropertyException(Config.Entry property, String reason) {
        this(property.name, reason);
    }
}
