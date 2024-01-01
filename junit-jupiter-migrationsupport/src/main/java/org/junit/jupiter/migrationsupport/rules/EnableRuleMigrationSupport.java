/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This class-level annotation enables native JUnit 4 rule support
 * within JUnit Jupiter.
 *
 * <p>Currently, rules of type {@code Verifier}, {@code ExternalResource},
 * and {@code ExpectedException} rules are supported.
 *
 * <p>{@code @EnableRuleMigrationSupport} is a composed annotation which
 * enables all supported extensions: {@link VerifierSupport},
 * {@link ExternalResourceSupport}, and {@link ExpectedExceptionSupport}.
 *
 * @since 5.0
 * @see ExternalResourceSupport
 * @see VerifierSupport
 * @see ExpectedExceptionSupport
 * @see org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@API(status = STABLE, since = "5.7")
@ExtendWith(ExternalResourceSupport.class)
@ExtendWith(VerifierSupport.class)
@ExtendWith(ExpectedExceptionSupport.class)
public @interface EnableRuleMigrationSupport {
}
