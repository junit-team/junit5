/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static java.util.Optional.ofNullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
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

class FullLogging implements BeforeAllCallback, AfterAllCallback {

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface LoggerSelector {
		Class<?>[] value();
	}

	private final Map<String, Level> previouslyActiveLogLevels = new HashMap<>();

	@Override
	public void beforeAll(ContainerExtensionContext context) throws Exception {
		// @formatter:off
        Class<?>[] loggerClasses = ofNullable(context.getTestClass().getAnnotation(LoggerSelector.class))
                .orElseThrow(() -> new IllegalArgumentException("There is no @LoggerSelector, add one!"))
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
