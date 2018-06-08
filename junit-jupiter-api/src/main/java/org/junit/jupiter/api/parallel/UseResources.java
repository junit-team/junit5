/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @UseResources} is a container for one or more
 * {@link UseResource @UseResource} declarations.
 *
 * <p>Note, however, that use of the {@code @UseResources} container is
 * completely optional since {@code @UseResource} is a
 * {@linkplain java.lang.annotation.Repeatable repeatable} annotation.
 *
 * @see UseResource
 * @since 5.3
 */
@API(status = EXPERIMENTAL, since = "5.3")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface UseResources {

	/**
	 * An array of one or more {@linkplain UseResource used resources}.
	 */
	UseResource[] value() default {};

}
