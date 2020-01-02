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
 * {@code @UseTechnicalNames} specifies that <em>technical names</em> should be
 * used instead of <em>display names</em> when running a test suite on the
 * JUnit Platform.
 *
 * <p>By default, <em>display names</em> will be used for test artifacts in
 * reports and graphical displays in IDEs; however, when a JUnit Platform test
 * suite is executed with a build tool such as Gradle or Maven, the generated
 * test report may need to include the <em>technical names</em> of test
 * artifacts &mdash; for example, fully qualified class names &mdash; instead
 * of shorter <em>display names</em> like the simple name of a test class or a
 * custom display name containing special characters.
 *
 * <p>Note that the presence of {@code @UseTechnicalNames} overrides any custom
 * display name configured for the suite via {@link SuiteDisplayName @SuiteDisplayName}.
 *
 * <h4>JUnit 4 Suite Support</h4>
 * <p>Test suites can be run on the JUnit Platform in a JUnit 4 environment via
 * {@code @RunWith(JUnitPlatform.class)}.
 *
 * @since 1.0
 * @see SuiteDisplayName
 * @see SelectPackages
 * @see SelectClasses
 * @see IncludeClassNamePatterns
 * @see ExcludeClassNamePatterns
 * @see IncludePackages
 * @see ExcludePackages
 * @see IncludeTags
 * @see ExcludeTags
 * @see IncludeEngines
 * @see ExcludeEngines
 * @see org.junit.platform.runner.JUnitPlatform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = MAINTAINED, since = "1.0")
public @interface UseTechnicalNames {
}
