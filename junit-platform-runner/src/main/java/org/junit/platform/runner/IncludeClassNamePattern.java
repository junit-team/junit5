/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.runner;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.discovery.ClassNameFilter;

/**
 * {@code @IncludeClassNamePattern} specifies a regular expression that is used
 * to match against fully qualified class names when running a test suite via
 * {@code @RunWith(JUnitPlatform.class)}.
 *
 * <p>If the fully qualified name of a class matches against the pattern, the
 * class will be included in the test plan.
 *
 * @since 1.0
 * @see JUnitPlatform
 * @see org.junit.platform.engine.discovery.ClassNameFilter#includeClassNamePatterns
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface IncludeClassNamePattern {

	/**
	 * Regular expression used to match against fully qualified class names.
	 *
	 * <p>Defaults to {@code "^.*Tests?$"} which matches against class names
	 * ending in {@code Test} or {@code Tests} (in any package).
	 */
	String value() default ClassNameFilter.STANDARD_INCLUDE_PATTERN;

}
