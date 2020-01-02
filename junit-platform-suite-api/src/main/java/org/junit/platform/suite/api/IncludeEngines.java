/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
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
 * {@code @IncludeEngines} specifies the {@linkplain #value IDs} of
 * {@link org.junit.platform.engine.TestEngine TestEngines} to be included
 * when running a test suite on the JUnit Platform.
 *
 * <h4>JUnit 4 Suite Support</h4>
 * <p>Test suites can be run on the JUnit Platform in a JUnit 4 environment via
 * {@code @RunWith(JUnitPlatform.class)}.
 *
 * @since 1.0
 * @see SuiteDisplayName
 * @see UseTechnicalNames
 * @see SelectPackages
 * @see SelectClasses
 * @see IncludeClassNamePatterns
 * @see ExcludeClassNamePatterns
 * @see IncludePackages
 * @see ExcludePackages
 * @see IncludeTags
 * @see ExcludeTags
 * @see ExcludeEngines
 * @see org.junit.platform.launcher.EngineFilter#includeEngines
 * @see org.junit.platform.runner.JUnitPlatform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = MAINTAINED, since = "1.0")
public @interface IncludeEngines {

	/**
	 * One or more TestEngine IDs to be included in the test plan.
	 */
	String[] value();

}
