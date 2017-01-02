/*
 * Copyright 2015-2017 the original author or authors.
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

/**
 * {@code @UseTechnicalNames} specifies that <em>technical names</em> should be
 * used instead of <em>display names</em> when running a test suite via
 * {@code @RunWith(JUnitPlatform.class)}.
 *
 * <p>By default, <em>display names</em> will be used for test artifacts; however,
 * when the {@link JUnitPlatform} runner is used to execute tests with a build
 * tool such as Gradle or Maven, the generated test report often needs to include
 * the <em>technical names</em> of test artifacts &mdash; for example, fully
 * qualified class names &mdash; instead of shorter <em>display names</em> like
 * the simple name of a test class or a custom display name containing special
 * characters. To enable <em>technical names</em>, simply declare the
 * {@code @UseTechnicalNames} annotation alongside {@code @RunWith(JUnitPlatform.class)}.
 *
 * @since 1.0
 * @see JUnitPlatform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface UseTechnicalNames {
}
