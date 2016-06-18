/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static org.junit.gen5.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.gen5.commons.meta.API;

/**
 * {@code @IncludeEngines} specifies the {@linkplain #value IDs} of
 * {@link org.junit.gen5.engine.TestEngine TestEngines} to be included
 * when running a test suite via {@code @RunWith(JUnitPlatform.class)}.
 *
 * @since 5.0
 * @see JUnitPlatform
 * @see ExcludeEngines
 * @see org.junit.gen5.launcher.EngineFilter#includeEngines
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@API(Maintained)
public @interface IncludeEngines {

	/**
	 * One or more Engine IDs to be included in the test plan.
	 */
	String[] value();

}
