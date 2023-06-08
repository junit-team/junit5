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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @ConfigurationParametersResource} is an annotation that specifies
 * a configuration file in property format to be added to the discovery request when running
 * a test suite on the JUnit Platform.
 *
 * @since 1.11
 * @see DisableParentConfigurationParameters
 * @see Suite
 * @see org.junit.platform.runner.JUnitPlatform
 * @see org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder#configurationParameter(String, String)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
// TODO: Before PR merge, change to STABLE/MAINTAINED
// TODO: Is version 1.11 correct?
@API(status = EXPERIMENTAL, since = "1.11")
@Repeatable(ConfigurationParametersResources.class)
public @interface ConfigurationParametersResource {

	/**
	 * The classpath location for the desired properties file; never {@code null} or blank.
	 */
	String resource();

}
