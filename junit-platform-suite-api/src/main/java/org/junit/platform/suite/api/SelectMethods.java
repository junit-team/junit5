/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @SelectClasses} specifies the methods to <em>select</em> when running
 * a test suite on the JUnit Platform.
 *
 * @see org.junit.platform.runner.JUnitPlatform
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@API(status = EXPERIMENTAL, since = "1.10")
public @interface SelectMethods {

	/**
	 * One or more methods to select.
	 *
	 * This annotation should be used to specify one or more methods to select.
	 * It can contain one or more @SelectMethod annotations, each specifying the claas,
	 * name and the parameterTypes of the method to select.
	 */

	SelectMethod[] value();
}
