/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DisabledOnOs} is used to signal that the annotated test class or
 * test method is <em>disabled</em> on one or more specified
 * {@linkplain #value operating systems}.
 *
 * <p>When applied at the class level, all test methods within that class
 * will be disabled on the same specified operating systems.
 *
 * @since 5.1
 * @see EnabledOnOs
 * @see org.junit.jupiter.api.Disabled
 * @see org.junit.jupiter.api.EnabledIf
 * @see org.junit.jupiter.api.DisabledIf
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(DisabledOnOsCondition.class)
@API(status = STABLE, since = "5.1")
public @interface DisabledOnOs {

	/**
	 * Operating systems on which the annotated class or method should be
	 * disabled.
	 *
	 * @see OS
	 */
	OS[] value();

}
