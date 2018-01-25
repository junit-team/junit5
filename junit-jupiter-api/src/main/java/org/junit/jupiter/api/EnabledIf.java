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

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @EnabledIf} is used to control whether the annotated test class or
 * test method is executed or not.
 *
 * <p>The decision is controlled by the return value of a script that is
 * evaluated:
 * <ul>
 * <li>{@code true} - if and only if the String-representation of the returned
 * value is parsed by {@link Boolean#parseBoolean(String)} to {@code true}.</li>
 * <li>{@code ConditionEvaluationResult} - an instance of
 *  {@link org.junit.jupiter.api.extension.ConditionEvaluationResult ConditionEvaluationResult}
 *  is passed directly to the framework.</li>
 *  </ul>
 *
 * <p>When this annotation with a script that evaluates to {@code false}
 * is applied at the class level, all test methods within that class
 * are automatically disabled as well.
 *
 * @since 5.1
 * @see org.junit.jupiter.api.extension.ExecutionCondition
 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#enabled(String)
 * @see org.junit.jupiter.api.extension.ConditionEvaluationResult#disabled(String)
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.1")
public @interface EnabledIf {

	/**
	 * Script predicate to evaluate.
	 *
	 * @return lines of the script predicate
	 */
	String[] value();

	/**
	 * Short name of the {@link javax.script.ScriptEngine ScriptEngine} to use.
	 *
	 * <p>An empty string is interpreted as {@code "javascript"}.
	 *
	 * @return script engine name
	 * @see javax.script.ScriptEngineManager#getEngineByName(String)
	 */
	String engine() default "";

	/**
	 * Delimiter separating script lines.
	 *
	 * @return the line separator or an empty String indicating {@link System#lineSeparator()}
	 */
	String delimiter() default "";

	/**
	 * Names to import.
	 *
	 * <p><b>JavaScript</b>
	 * {@code JavaImporter} takes a variable number of arguments
	 * as Java packages, and the returned object is used in a {@code with} statement
	 * whose scope includes the specified package imports. The global JavaScript
	 * scope is not affected.
	 *
	 * <p><b>Groovy</b>
	 * Each name is inserted as {@code import name} at the top of the script.
	 *
	 * @return names to import
	 */
	String[] imports() default {};

	/**
	 * Bind the active {@link org.junit.jupiter.api.extension.ExtensionContext ExtensionContext}
	 * to {@code jupiterExtensionContext}.
	 *
	 * @return name of binding, an empty String prevents the binding
	 * @see javax.script.Bindings
	 */
	String bindExtensionContext() default "jupiterExtensionContext";

	/**
	 * Bind {@link System#getProperties()} to {@code systemProperties}.
	 *
	 * @return name of binding, an empty String prevents the binding
	 * @see javax.script.Bindings
	 */
	String bindSystemProperties() default "systemProperties";

	/**
	 * Reason why the container or test should be enabled.
	 *
	 * @return the reason why the container or test should be enabled
	 */
	String reason() default "";

}
