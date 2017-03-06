/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @Tag} is a {@linkplain Repeatable repeatable} annotation that is
 * used to declare a <em>tag</em> for the annotated test class or test method.
 *
 * <p>Tags are used to filter which tests are executed for a given test
 * plan. For example, a development team may tag tests with values such as
 * {@code "fast"}, {@code "slow"}, {@code "ci-server"}, etc. and then supply
 * a list of tags to be used for the current test plan, potentially
 * dependent on the current environment.
 *
 * @since 5.0
 * @see Tags
 * @see Test
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Tags.class)
@API(Maintained)
public @interface Tag {

	/**
	 * The <em>tag</em>.
	 */
	String value();

}
