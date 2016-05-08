/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.gen5.api.extension.AfterAllCallback;
import org.junit.gen5.api.extension.BeforeAllCallback;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.commons.util.PreconditionViolationException;

/**
 * Class-level extension that turns on full logging (i.e., {@link Level#ALL})
 * for all specified classes for the duration of the current test container.
 *
 * <h3>Example Usage</h3>
 * <pre style="code">
 * {@literal @}FullLogging(MethodInvoker.class)
 * class MyTestClass { ... }
 * </pre>
 *
 * @since 5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FullLogging.Extension.class)
public @interface FullLogging {

	Class<?>[] value();

	static class Extension implements BeforeAllCallback, AfterAllCallback {

		private final Map<String, Level> previouslyActiveLogLevels = new HashMap<>();

		@Override
		public void beforeAll(ContainerExtensionContext context) throws Exception {
		// @formatter:off
		Class<?>[] loggerClasses = findAnnotation(context.getTestClass(), FullLogging.class)
				.orElseThrow(() -> new PreconditionViolationException("@FullLogging must be declared on class " +
						context.getTestClass().getName()))
				.value();

		Arrays.stream(loggerClasses)
				.map(Class::getName)
				.forEach(loggerName -> {
					Logger logger = Logger.getLogger(loggerName);
					previouslyActiveLogLevels.put(loggerName, logger.getLevel());
					logger.setLevel(Level.ALL);
				});
		// @formatter:on
		}

		@Override
		public void afterAll(ContainerExtensionContext context) throws Exception {
			previouslyActiveLogLevels.forEach(
				(loggerName, previousLogLevel) -> Logger.getLogger(loggerName).setLevel(previousLogLevel));
		}

	}

}
