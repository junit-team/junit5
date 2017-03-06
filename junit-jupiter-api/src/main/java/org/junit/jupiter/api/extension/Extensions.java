/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @Extensions} is a container for one or more {@code @ExtendWith}
 * declarations.
 *
 * <p>Note, however, that use of the {@code @Extensions} container is completely
 * optional since {@code @ExtendWith} is a {@linkplain java.lang.annotation.Repeatable
 * repeatable} annotation.
 *
 * @since 5.0
 * @see ExtendWith
 * @see java.lang.annotation.Repeatable
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(Experimental)
public @interface Extensions {

	/**
	 * An array of one or more {@link ExtendWith @ExtendWith} declarations.
	 */
	ExtendWith[] value();

}
