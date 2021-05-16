/*
 * Copyright 2015-2021 the original author or authors.
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
import org.junit.platform.commons.annotation.Testable;

/**
 * {@code @Suite} marks class as a test suite on the JUnit Platform.
 * <p>Selector and filter annotations are used to control the contents of the
 * suite. Additionally configuration can be passed to the suite via the
 * configuration annotations.
 *
 * <p>When the {@link IncludeClassNamePatterns @IncludeClassNamePatterns}
 * annotation is not present, the default include pattern
 * {@value org.junit.platform.engine.discovery.ClassNameFilter#STANDARD_INCLUDE_PATTERN}
 * will be used in order to avoid loading classes unnecessarily (see {@link
 * org.junit.platform.engine.discovery.ClassNameFilter#STANDARD_INCLUDE_PATTERN
 * ClassNameFilter#STANDARD_INCLUDE_PATTERN}).
 *
 * @since 1.8
 * @see SelectClasses
 * @see SelectClasspathResource
 * @see SelectDirectories
 * @see SelectFile
 * @see SelectModules
 * @see SelectPackages
 * @see SelectUris
 * @see IncludeClassNamePatterns
 * @see ExcludeClassNamePatterns
 * @see IncludeEngines
 * @see ExcludeEngines
 * @see IncludePackages
 * @see ExcludePackages
 * @see IncludeTags
 * @see ExcludeTags
 * @see SuiteDisplayName
 * @see ConfigurationParameter
 * @see EnableImplicitConfigurationParameters
 * @see org.junit.platform.launcher.LauncherDiscoveryRequest
 * @see org.junit.platform.launcher.LauncherDiscoveryRequestBuilder
 * @see org.junit.platform.launcher.Launcher
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = Status.EXPERIMENTAL, since = "1.8")
@Testable
public @interface Suite {
}
