/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.suppliers;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * Parameter argument supplier annotation that can be added to a theory
 * parameter to specify the exact values that will be used for that parameter.
 * Provides {@code float} values.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSuppliedBy(FloatTheoryArgumentSupplier.class)
@API(status = EXPERIMENTAL, since = "5.2")
public @interface FloatValues {
	/**
	 * @return the value(s) to use for the annotated theory parameter
	 */
	float[] value();
}
