package io.matshou.cata.tilecov.config;

public class MissingConfigProperty extends RuntimeException {

    MissingConfigProperty(String property) {
        super(String.format("%s: property not defined in config file.", property));
    }
}
