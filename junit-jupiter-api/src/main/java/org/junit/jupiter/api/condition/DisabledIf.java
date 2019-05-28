/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.DEPRECATED;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @DisabledIf} is used to determine whether the annotated test class or
 * test method is <em>disabled</em> by evaluating a script.
 *
 * <p>The decision is made by interpreting the return value of the supplied
 * {@linkplain #value script}, according to the following table.
 *
 * <table class="plain">
 * <tr>
 *   <th>Return Type</th>
 *   <th>Evaluation Result</th>
 * </tr>
 * <tr>
 *   <td>{@code boolean}</td>
 *   <td>The annotated element will be disabled if the value is {@code true}.</td>
 * </tr>
 * <tr>
 *   <td>{@code java.lang.Boolean}</td>
 *   <td>The annotated element will be disabled if the value is {@code Boolean.TRUE}.</td>
 * </tr>
 * <tr>
 *   <td>{@code ConditionEvaluationResult}</td>
 *   <td>An instance of {@link org.junit.jupiter.api.extension.ConditionEvaluationResult
 *       ConditionEvaluationResult} will be handled directly by JUnit Jupiter as if the
 *       script were an implementation of {@link org.junit.jupiter.api.extension.ExecutionCondition
 *       ExecutionCondition}.</td>
 * </tr>
 * <tr>
 *    <td>{@code null}</td>
 *    <td>A return value of {@code null} is considered to be an error and will
 *        result in a {@link org.junit.jupiter.api.extension.ScriptEvaluationException
 *        ScriptEvaluationException}.</td>
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
 * <p>If a test class is disabled via the evaluation of {@code @DisabledIf}, all
 * test methods within that class are automatically disabled as well.
 *
 * <p>If a test method is disabled via this annotation, that does not prevent
 * the test class from being instantiated. Rather, it prevents the execution of
 * the test method and method-level lifecycle callbacks such as {@code @BeforeEach}
 * methods, {@code @AfterEach} methods, and corresponding extension APIs.
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
 * <p>The following {@link javax.script.Bindings bindings} are available for
 * accessing information from the JUnit Jupiter
 * {@link org.junit.jupiter.api.extension.ExtensionContext ExtensionContext}.
 *
 * <ul>
 * <li>{@code junitTags}: All tags as a {@code Set<String>}</li>
 * <li>{@code junitDisplayName}: Display name as a {@code String}</li>
 * <li>{@code junitUniqueId}: Unique ID as a {@code String}</li>
 * <li>{@code junitConfigurationParameter}: Configuration parameter accessor</li>
 * </ul>
 *
 * <p>Scripts must not declare variables using names that start with {@code junit},
 * since they might clash with bindings provided by JUnit.
 *
 * <p>This annotation may be used as a meta-annotation in order to create a
 * custom <em>composed annotation</em> that inherits the semantics of this
 * annotation.
 *
 * <h4>Warning</h4>
 *
 * <p>As of JUnit Jupiter 5.1, this annotation can only be declared once on an
 * {@link java.lang.reflect.AnnotatedElement AnnotatedElement} (i.e., test
 * interface, test class, or test method). If this annotation is directly
 * present, indirectly present, or meta-present multiple times on a given
 * element, only the first such annotation discovered by JUnit will be used;
 * any additional declarations will be silently ignored. Note, however, that
 * this annotation may be used in conjunction with other {@code @Enabled*} or
 * {@code @Disabled*} annotations in this package.
 *
 * @deprecated Script-based condition APIs and their supporting implementations
 * are deprecated with the intent to remove them in JUnit Jupiter 5.6. Users
 * should instead rely on a combination of other built-in conditions or create
 * and use a custom implementation of
 * {@link org.junit.jupiter.api.extension.ExecutionCondition ExecutionCondition}
 * to evaluate the same conditions.
 *
 * @since 5.1
 * @see org.junit.jupiter.api.condition.EnabledIf
 * @see org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.condition.EnabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.DisabledIfSystemProperty
 * @see org.junit.jupiter.api.condition.EnabledOnJre
 * @see org.junit.jupiter.api.condition.DisabledOnJre
 * @see org.junit.jupiter.api.condition.EnabledOnOs
 * @see org.junit.jupiter.api.condition.DisabledOnOs
 * @see org.junit.jupiter.api.Disabled
 * @see org.junit.jupiter.api.extension.ExecutionCondition
 * @see javax.script.ScriptEngine
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = DEPRECATED, since = "5.5")
@Deprecated
public @interface DisabledIf {

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
	 *   <li><code>{annotation}</code>: the String representation of the {@code @DisabledIf} annotation instance</li>
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
