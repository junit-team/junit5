/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @OnlyEngine} specifies the engine ID (e.g., {@code "junit5"}) of
 * the sole {@link org.junit.gen5.engine.TestEngine TestEngine} to be used
 * when a class annotated with {@code @RunWith(JUnit5.class)} is run.
 *
 * @since 5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface OnlyEngine {

	/**
	 * The Engine ID.
	 */
	String value();

}
