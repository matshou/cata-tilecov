package io.matshou.cata.tilecov.config;

public class MissingConfigPropertyException extends RuntimeException {

    MissingConfigPropertyException(String property) {
        super(String.format("%s: property not defined in config file.", property));
    }
}
