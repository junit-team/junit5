/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @EnabledIf} evaluates a script signalling that the annotated
 * test class or test method is currently <em>enabled</em> or <em>disabled</em>.
 *
 * @since 5.1
 * @see org.junit.jupiter.api.extension.ExecutionCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.1")
public @interface EnabledIf {

	/**
	 * The script controlling the <em>enabled</em> or <em>disabled</em> state.
	 */
	String[] value();

	/**
	 * The reason this test is <em>enabled</em> or <em>disabled</em>.
	 *
	 * <p>Defaults to: {@code "Script `{script}` evaluated to: {result}"}
	 * <ul>
	 *     <li>{@code {annotation}} the String-representation of the {@code @EnabledIf} annotation instance</li>
	 *     <li>{@code {script}} the script text that was evaluated</li>
	 *     <li>{@code {result}} the String-representation of the result object returned by the script</li>
	 * </ul>
	 * @return reason message
	 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#getReason()
	 */
	String reason() default "Script `{script}` evaluated to: {result}";

	/**
	 * Names used for the script {@link javax.script.Bindings bindings}.
	 */
	interface Bind {

		/**
		 * Set of all tags assigned to the current extension context.
		 *
		 * <p>Value type: {@code Set<String>}
		 *
		 * @see org.junit.jupiter.api.extension.ExtensionContext#getTags()
		 */
		String JUPITER_TAGS = "jupiterTags";

		/**
		 * Unique ID associated with the current extension context.
		 *
		 * <p>Value type: {@code String}
		 *
		 * @see org.junit.jupiter.api.extension.ExtensionContext#getUniqueId()
		 */
		String JUPITER_UNIQUE_ID = "jupiterUniqueId";

		/**
		 * Display name of the test or container.
		 *
		 * <p>Value type: {@code String}
		 *
		 * @see org.junit.jupiter.api.extension.ExtensionContext#getDisplayName()
		 */
		String JUPITER_DISPLAY_NAME = "jupiterDisplayName";

		/**
		 * Configuration parameter stored under the specified key.
		 *
		 * <p>Usage: {@code jupiterConfigurationParameter.get(key) -> String}
		 *
		 * @see org.junit.jupiter.api.extension.ExtensionContext#getConfigurationParameter(String)
		 */
		String JUPITER_CONFIGURATION_PARAMETER = "jupiterConfigurationParameter";

		/**
		 * System property stored under the specified key.
		 *
		 * <p>Usage: {@code systemProperty.get(key) -> String}
		 *
		 * @see System#getProperty(String)
		 */
		String SYSTEM_PROPERTY = "systemProperty";

		/**
		 * System environment value stored under the specified key.
		 *
		 * <p>Usage: {@code systemEnvironment.get(key) -> String}
		 *
		 * @see System#getenv(String)
		 */
		String SYSTEM_ENVIRONMENT = "systemEnvironment";

	}
}
