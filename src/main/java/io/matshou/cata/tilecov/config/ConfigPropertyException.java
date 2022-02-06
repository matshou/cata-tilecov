package io.matshou.cata.tilecov.config;

/**
 * This exception should be thrown when {@link Config} properties are malformed or missing.
 */
public class ConfigPropertyException extends RuntimeException {

    ConfigPropertyException(String reason) {
        super(reason);
    }
}
