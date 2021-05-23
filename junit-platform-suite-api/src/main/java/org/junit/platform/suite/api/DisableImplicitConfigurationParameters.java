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

/**
 * Disable implicit configuration parameters.
 *
 * <p>By default, in addition to those parameters that are passed explicitly
 * by {@link @ConfigurationParameter}, configuration parameters are read from
 * system properties and from the {@code junit-platform.properties} classpath
 * resource.
 *
 * Annotating a suite with this annotation disables the latter two sources so
 * that only explicit configuration parameters are taken into account.
 *
 * @see ConfigurationParameter
 * @see Suite
 * @see org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder#enableImplicitConfigurationParameters(boolean)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(status = Status.EXPERIMENTAL, since = "1.8")
public @interface DisableImplicitConfigurationParameters {

}
