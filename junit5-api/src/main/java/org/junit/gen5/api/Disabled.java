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
import java.util.Optional;
import java.util.function.Function;

import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.StringUtils;

/**
 * {@code @Disabled} is used to signal that the annotated test class or
 * test method is currently <em>disabled</em> and should not be executed.
 *
 * <p>When applied at the class level, all test methods within that class
 * are automatically disabled as well.
 *
 * @author Sam Brannen
 * @since 5.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(Disabled.DisabledCondition.class)
public @interface Disabled {

	/**
	 * The reason this test is disabled.
	 */
	String value() default "";

	static class DisabledCondition implements Condition {

		/**
		 * Tests are disabled if {@code @Disabled} is either present on the
		 * test class or on the test method.
		 */
		@Override
		public Result evaluate(TestExecutionContext context) {

			// Class level?
			Optional<Disabled> disabled = findAnnotation(context.getTestClass(), Disabled.class);
			Function<TestExecutionContext, String> reasonBuilder = DisabledCondition::buildClassLevelReason;

			// Method level?
			if (!disabled.isPresent()) {
				disabled = findAnnotation(context.getTestMethod(), Disabled.class);
				reasonBuilder = DisabledCondition::buildMethodLevelReason;
			}

			if (disabled.isPresent()) {
				String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElse(
					reasonBuilder.apply(context));
				return Result.failure(reason);
			}

			return Result.success("@Disabled is not present");
		}

		private static String buildClassLevelReason(TestExecutionContext context) {
			return String.format("@Disabled is present on test class [%s]", context.getTestClass().get().getName());
		}

		private static String buildMethodLevelReason(TestExecutionContext context) {
			return String.format("@Disabled is present on test method [%s]",
				context.getTestMethod().get().toGenericString());
		}

	}

}
