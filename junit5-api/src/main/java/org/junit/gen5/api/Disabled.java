/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.gen5.api.extension.TestExecutionContext;

/**
 * @author Sam Brannen
 * @since 5.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(Disabled.DisabledCondition.class)
public @interface Disabled {

	static class DisabledCondition implements Condition {

		/**
		 * Tests are enabled if {@code @Disabled} is neither present on the
		 * test class nor on the test method.
		 */
		@Override
		public boolean matches(TestExecutionContext context) {
			// @formatter:off
			return context.getTestClass().map(clazz -> !findAnnotation(clazz, Disabled.class).isPresent()).orElse(true)
					&& context.getTestMethod().map(method -> !findAnnotation(method, Disabled.class).isPresent()).orElse(true);
			// @formatter:on
		}
	}

}
