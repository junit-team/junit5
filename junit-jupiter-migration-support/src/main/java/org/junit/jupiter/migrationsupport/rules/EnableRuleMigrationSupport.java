/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.meta.API;

/**
 * This class-level annotation turns on native JUnit 4 rule support
 * within JUnit Jupiter.
 * Currently, rules of type {@code Verifier} and
 * {@code ExternalResource} are supported.
 * {@code EnableRuleMigrationSupport} is a meta-annotation which
 * includes both supported extensions: {@code VerifierSupport} and
 * {@code ExternalResourceSupport}.
 *
 * @since 5.0
 * @see ExternalResourceSupport
 * @see VerifierSupport
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@API(Experimental)
@ExtendWith(ExternalResourceSupport.class)
@ExtendWith(VerifierSupport.class)
public @interface EnableRuleMigrationSupport {
}
