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
 * {@code @EnabledIf} evaluates a script controlling that the annotated
 * test class or test method is currently <em>enabled</em> or <em>disabled</em>.
 *
 * <p>The decision is made by interpreting the return value of the {@link #value() script}:
 * <ul>
 * <li>{@code true} - if and only if the String-representation of the returned
 * value is parsed by {@link Boolean#parseBoolean(String)} to {@code true}.</li>
 * <li>{@code ConditionEvaluationResult} - an instance of
 *  {@link org.junit.jupiter.api.extension.ConditionEvaluationResult ConditionEvaluationResult}
 *  is passed directly to the Jupiter engine.</li>
 *  </ul>
 *
 * <p>When this annotation with a script that evaluates to {@code false}
 * is applied at the class level, all test methods within that class
 * are automatically disabled as well.
 *
 * <h3>Script engine</h3>
 * The default script engine is: Oracle Nashorn.
 *
 * The {@link #engine()} property overrides the default script engine name.
 *
 * <h3>Bindings</h3>
 * An {@link Accessor accessor} value provides access to a map-like structure.
 *
 * <p>The following System property accessors are available:
 * <ul>
 * <li>{@link Bind#SYSTEM_ENVIRONMENT systemEnvironment} - System environment accessor.</li>
 * <li>{@link Bind#SYSTEM_PROPERTY systemProperty} - System property accessor.</li>
 * </ul>
 *
 * The following Jupiter extension context-aware {@link Bind bindings} are available:
 * <ul>
 * <li>{@link Bind#JUPITER_TAGS jupiterTags} - All tags as a {@code Set<String>}.</li>
 * <li>{@link Bind#JUPITER_DISPLAY_NAME jupiterDisplayName} - Display name as a {@code String}.</li>
 * <li>{@link Bind#JUPITER_UNIQUE_ID jupiterUniqueId} - Unique ID as a {@code String}.</li>
 * <li>{@link Bind#JUPITER_CONFIGURATION_PARAMETER jupiterConfigurationParameter} - Configuration parameter accessor.</li>
 * </ul>
 *
 * @since 5.1
 * @see javax.script.ScriptEngine
 * @see org.junit.jupiter.api.extension.ExecutionCondition
 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#enabled(String)
 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#disabled(String)
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.1")
public @interface EnabledIf {

	/**
	 * The lines of the script controlling the <em>enabled</em> or <em>disabled</em> state.
	 */
	String[] value();

	/**
	 * Short name of the {@link javax.script.ScriptEngine ScriptEngine} to use.
	 *
	 * <p>Oracle Nashorn is used by default interpreting JavaScript scripts.
	 *
	 * <p>Until Java SE 7, JDKs shipped with a JavaScript scripting engine based
	 * on Mozilla Rhino. Java SE 8 instead ships with the new engine called
	 * Oracle Nashorn, which is based on JSR 292 and {@code invokedynamic}.
	 *
	 * @return script engine name
	 * @see javax.script.ScriptEngineManager#getEngineByName(String)
	 * @see <a href="http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html">Oracle Nashorn</a>
	 */
	String engine() default "nashorn";

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
	 * Used to access named properties without exposing the underlying container (map).
	 */
	interface Accessor {

		/**
		 * Get the value assigned to the specified name.
		 *
		 * @param key the name to lookup
		 * @return the value assigned to the specified name; maybe {@code null}
		 */
		String get(String key);

	}

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
