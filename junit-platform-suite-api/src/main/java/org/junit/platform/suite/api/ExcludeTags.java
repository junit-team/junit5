/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.api;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @ExcludeTags} specifies the {@linkplain #value tags} to be excluded
 * when running a test suite on the JUnit Platform.
 *
 * <h3>Syntax Rules for Tags</h3>
 * <ul>
 * <li>A tag must not be blank.</li>
 * <li>A trimmed tag must not contain whitespace.</li>
 * <li>A trimmed tag must not contain ISO control characters.</li>
 * </ul>
 *
 * <h4>JUnit 4 Suite Support</h4>
 * <p>Test suites can be run on the JUnit Platform in a JUnit 4 environment via
 * {@code @RunWith(JUnitPlatform.class)}.
 *
 * @since 1.0
 * @see IncludeTags
 * @see org.junit.platform.launcher.TagFilter#excludeTags
 * @see org.junit.platform.runner.JUnitPlatform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = MAINTAINED, since = "1.0")
public @interface ExcludeTags {

	/**
	 * One or more tags to exclude.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}.
	 */
	String[] value();

}
