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
 * {@code @EnabledIf} is used to control whether the annotated test class or
 * test method is <em>enabled</em> or <em>disabled</em> by evaluating a script.
 *
 * <p>The decision is made by interpreting the return value of the supplied
 * {@linkplain #value script}, according to the following table.
 *
 * <table border="1">
 * <tr>
 *   <th>Return Type</th>
 *   <th>Evaluation Result</th>
 * </tr>
 * <tr>
 *   <td>{@code boolean}</td>
 *   <td>The annotated element will be enabled if the value is {@code true}.</td>
 * </tr>
 * <tr>
 *   <td>{@code java.lang.Boolean}</td>
 *   <td>The annotated element will be enabled if the value is {@code Boolean.TRUE}.</td>
 * </tr>
 * <tr>
 *   <td>{@code ConditionEvaluationResult}</td>
 *   <td>An instance of {@link org.junit.jupiter.api.extension.ConditionEvaluationResult
 *       ConditionEvaluationResult} will be handled directly by JUnit Jupiter as if the
 *       script were an implementation of {@link org.junit.jupiter.api.extension.ExecutionCondition
 *       ExecutionCondition}.</td>
 * </tr>
 * <tr>
 *   <td>*</td>
 *   <td>The value of any other return type will be converted to its String
 *       representation by {@link String#valueOf(Object)} and then interpreted as
 *       a boolean by passing the String representation to
 *       {@link Boolean#parseBoolean(String)}.</td>
 * </tr>
 * </table>
 *
 * <p>If a test class is disabled via the evaluation of {@code @EnabledIf}, all
 * test methods within that class are automatically disabled as well.
 *
 * <h3>Script Engines</h3>
 *
 * <p>The default script engine is <em>Oracle Nashorn</em>; however, the
 * {@link #engine} attribute may be used to override the default script engine
 * name.
 *
 * <h3>Bindings</h3>
 *
 * <p>An <em>accessor</em> provides access to a map-like structure via a simple
 * {@code String get(String name)} method. The following property accessors are
 * automatically available within scripts.
 *
 * <ul>
 * <li>{@code systemEnvironment}: Operating system environment variable accessor</li>
 * <li>{@code systemProperty}: JVM system property accessor</li>
 * </ul>
 *
 * <p>The following {@link javax.script.Bindings bindings} are available for accessing information
 * from the JUnit Jupiter {@link org.junit.jupiter.api.extension.ExtensionContext
 * ExtensionContext}.
 *
 * <ul>
 * <li>{@code junitTags}: All tags as a {@code Set<String>}</li>
 * <li>{@code junitDisplayName}: Display name as a {@code String}</li>
 * <li>{@code junitUniqueId}: Unique ID as a {@code String}</li>
 * <li>{@code junitConfigurationParameter}: Configuration parameter accessor</li>
 * </ul>
 *
 * <p>Scripts must not declare variables using names that start with {@code junit}.
 * They might clash with new bindings introduced in the future.
 *
 * @since 5.1
 * @see Disabled
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
	 * The lines of the script to evaluate.
	 */
	String[] value();

	/**
	 * The reason this annotated test class or test method is <em>enabled</em>
	 * or <em>disabled</em>.
	 *
	 * <p>Defaults to: <code>"Script `{script}` evaluated to: {result}"</code>.
	 *
	 * <h5>Supported placeholders</h5>
	 * <ul>
	 *   <li><code>{annotation}</code>: the String representation of the {@code @EnabledIf} annotation instance</li>
	 *   <li><code>{script}</code>: the script text that was evaluated</li>
	 *   <li><code>{result}</code>: the String representation of the return value of the evaluated script</li>
	 * </ul>
	 *
	 * @return the reason the element is enabled or disabled
	 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#getReason()
	 */
	String reason() default "Script `{source}` evaluated to: {result}";

	/**
	 * Short name of the {@link javax.script.ScriptEngine ScriptEngine} to use.
	 *
	 * <p>Oracle Nashorn is used by default, providing support for evaluating
	 * JavaScript scripts.
	 *
	 * <p>Until Java SE 7, JDKs shipped with a JavaScript scripting engine based
	 * on Mozilla Rhino. Java SE 8 instead ships with the new engine called
	 * Oracle Nashorn, which is based on JSR 292 and {@code invokedynamic}.
	 *
	 * @return script engine name
	 * @see javax.script.ScriptEngineManager#getEngineByName(String)
	 * @see <a href="http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html">Oracle Nashorn</a>
	 */
	String engine() default "Nashorn";

}
