package io.matshou.cata.tilecov.json;

import java.lang.annotation.*;

/**
 * An annotation that indicates this member should be serialized to JSON with the provided name value
 * as its field name. It should be used to annotate fields that represent JSON properties that
 * need to be deserialized into Class objects other than {@code String} or {@code List}.
 * <p>
 * Members annotated with {@code SerializedObjectName} are expected to be declared as {@link java.util.List}
 * otherwise the application will throw a {@link RuntimeException} during deserialization.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SerializedObjectName {

    /**
     * @return the desired name of the field when it is serialized or deserialized.
     */
    String value();
}
