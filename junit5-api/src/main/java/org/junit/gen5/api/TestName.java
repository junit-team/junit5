/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @TestName} is used to inject the display name of the current test
 * into to {@code @Test}, {@code @BeforeEach}, and {@code @AfterEach} methods.
 *
 * <p>If a method parameter is of type {@link String} and annotated with
 * {@code @TestName}, JUnit will supply the display name of the current
 * test as the value for the annotated parameter.
 *
 * <p>The display name is either the canonical name of the test or a custom
 * name configured via {@link Name @Name}.
 *
 * @author Matthias Merdes
 * @author Johannes Link
 * @author Sam Brannen
 * @since 5.0
 * @see Test
 * @see Name
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestName {
}
