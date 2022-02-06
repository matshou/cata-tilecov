package io.matshou.cata.tilecov.config;

public class IllegalConfigProperty extends ConfigPropertyException {

    IllegalConfigProperty(Config.Entry property, String reason) {
        super(String.format("%s: %s", property.name, reason));
    }
}
