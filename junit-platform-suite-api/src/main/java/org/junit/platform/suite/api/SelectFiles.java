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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * {@code @SelectFiles} is a container for one or more
 * {@link SelectFile @SelectFile} declarations.
 *
 * <p>Note, however, that use of the {@code @SelectFiles} container is
 * completely optional since {@code @SelectFile} is a
 * {@linkplain java.lang.annotation.Repeatable repeatable} annotation.
 *
 * @since 1.8
 * @see SelectFile
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = Status.EXPERIMENTAL, since = "1.8")
public @interface SelectFiles {

	/**
	 * An array of one or more {@link SelectFile @SelectFile} declarations.
	 */
	SelectFile[] value();

}
