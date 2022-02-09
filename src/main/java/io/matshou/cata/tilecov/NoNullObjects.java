package io.matshou.cata.tilecov;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Nonnull
@Documented
@Retention(RUNTIME)
@Target({ TYPE, PACKAGE })
@TypeQualifierDefault({ METHOD, PARAMETER })
public @interface NoNullObjects {}
