/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Class-level extension that turns on full logging (i.e., {@link Level#ALL})
 * for all specified classes for the duration of the current test container.
 *
 * <h3>Example Usage</h3>
 * <pre class="code">
 * {@literal @}FullLogging(classes = ExecutableInvoker.class)
 * class MyTestClass { ... }
 * </pre>
 *
 * @since 5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FullLogging.Extension.class)
public @interface FullLogging {

	Class<?>[] classes() default FullLogging.class;

	String[] classNames() default "";

	class Extension implements BeforeAllCallback, AfterAllCallback {

		private final Map<String, Level> previouslyActiveLogLevels = new HashMap<>();

		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			Class<?> testClass = context.getRequiredTestClass();

			// @formatter:off
			FullLogging fullLogging = findAnnotation(testClass, FullLogging.class)
					.orElseThrow(() -> new PreconditionViolationException("@FullLogging must be declared on class " +
							testClass.getName()));
			// @formatter:on

			Class<?>[] classes = fullLogging.classes();
			String[] classNames = fullLogging.classNames();
			List<String> loggerNames = new ArrayList<>();

			if (classes.length > 0 && classes[0] != FullLogging.class) {
				loggerNames.addAll(Arrays.stream(classes).map(Class::getName).collect(toList()));
			}

			if (classNames.length > 0 && !classNames[0].equals("")) {
				loggerNames.addAll(Arrays.asList(classNames));
			}

			// @formatter:off
			Arrays.stream(classNames)
					.forEach(loggerName -> {
						Logger logger = Logger.getLogger(loggerName);
						this.previouslyActiveLogLevels.put(loggerName, logger.getLevel());
						logger.setLevel(Level.ALL);
					}
			);
			// @formatter:on
		}

		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			this.previouslyActiveLogLevels.forEach(
				(loggerName, previousLogLevel) -> Logger.getLogger(loggerName).setLevel(previousLogLevel));
		}

	}

}
