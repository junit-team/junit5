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

import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;

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
		 * Tests are disabled if {@code @Disabled} is either present on the
		 * test class or on the test method.
		 */
		@Override
		public Result evaluate(TestExecutionContext context) {

			if (isAnnotated(context.getTestClass(), Disabled.class)) {
				return Result.failure(
					String.format("@Disabled is present on test class [%s]", context.getTestClass().get().getName()));
			}

			if (isAnnotated(context.getTestMethod(), Disabled.class)) {
				return Result.failure(String.format("@Disabled is present on test method [%s]",
					context.getTestMethod().get().toGenericString()));
			}

			return Result.success("@Disabled is not present");
		}
	}

}
