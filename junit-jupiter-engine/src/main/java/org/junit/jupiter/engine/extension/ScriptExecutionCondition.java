/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.script.Script;
import org.junit.platform.commons.util.BlacklistedExceptions;

/**
 * {@link ExecutionCondition} that supports the {@link DisabledIf} and {@link EnabledIf} annotation.
 *
 * @since 5.1
 * @see DisabledIf
 * @see EnabledIf
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
@Deprecated
class ScriptExecutionCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_NO_ELEMENT = enabled("AnnotatedElement not present");

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled("Annotation not present");

	private static final String EVALUATOR_CLASS_NAME = "org.junit.jupiter.engine.extension.ScriptExecutionEvaluator";

	private final Evaluator evaluator;

	// Used by the ExtensionRegistry.
	ScriptExecutionCondition() {
		this(EVALUATOR_CLASS_NAME);
	}

	// Used by tests.
	ScriptExecutionCondition(String evaluatorImplementationName) {
		this.evaluator = Evaluator.forName(evaluatorImplementationName);
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		// Context without an annotated element?
		Optional<AnnotatedElement> element = context.getElement();
		if (!element.isPresent()) {
			return ENABLED_NO_ELEMENT;
		}
		AnnotatedElement annotatedElement = element.get();

		// Always try to create script instances.
		Script disabledScript = createDisabledIfScriptOrNull(annotatedElement);
		Script enabledScript = createEnabledIfScriptOrNull(annotatedElement);

		// If no scripts are created, no annotation of interest is attached to the underlying element.
		if (disabledScript == null && enabledScript == null) {
			return ENABLED_NO_ANNOTATION;
		}

		// Prepare list with single or two script elements.
		List<Script> scripts = new ArrayList<>();
		if (disabledScript != null) {
			scripts.add(disabledScript);
		}
		if (enabledScript != null) {
			scripts.add(enabledScript);
		}

		// Let the evaluator do its work.
		return evaluator.evaluate(context, scripts);
	}

	private Script createDisabledIfScriptOrNull(AnnotatedElement annotatedElement) {
		Optional<DisabledIf> disabled = findAnnotation(annotatedElement, DisabledIf.class);
		if (!disabled.isPresent()) {
			return null;
		}
		DisabledIf annotation = disabled.get();
		String source = createSource(annotation.value());
		return new Script(annotation, annotation.engine(), source, annotation.reason());
	}

	private Script createEnabledIfScriptOrNull(AnnotatedElement annotatedElement) {
		Optional<EnabledIf> enabled = findAnnotation(annotatedElement, EnabledIf.class);
		if (!enabled.isPresent()) {
			return null;
		}
		EnabledIf annotation = enabled.get();
		String source = createSource(annotation.value());
		return new Script(annotation, annotation.engine(), source, annotation.reason());
	}

	private String createSource(String[] lines) {
		return String.join(System.lineSeparator(), lines);
	}

	/**
	 * Evaluates scripts and returns a conditional evaluation result.
	 */
	interface Evaluator {

		ConditionEvaluationResult evaluate(ExtensionContext context, List<Script> scripts);

		/**
		 * Create evaluator via reflection to hide the `javax.script` dependency.
		 *
		 * <p>This method may return a {@link ThrowingEvaluator} instance on JREs that
		 * don't provide the "javax.script" package at all. It also returns such an
		 * instance on JREs that are launched with an active module system using
		 * insufficient module graphs, i.e. the application does not read
		 * {@code java.scripting} module.
		 *
		 * @see Class#forName(String)
		 */
		static Evaluator forName(String name) {
			// Assert that "javax.script.ScriptEngine" is loadable via basic reflection.
			return forName("javax.script.ScriptEngine", name);
		}

		static Evaluator forName(String nameOfScriptEngine, String name) {
			// Assert that precondition name is loadable via basic reflection.
			try {
				Class.forName(nameOfScriptEngine);
			}
			catch (Throwable cause) {
				BlacklistedExceptions.rethrowIfBlacklisted(cause);
				String message = "Class `" + nameOfScriptEngine + "` is not loadable, " //
						+ "script-based test execution is disabled. " //
						+ "If the originating cause is a `NoClassDefFoundError: javax/script/...` and " //
						+ "the underlying runtime environment is executed with an activated module system " //
						+ "(aka Jigsaw or JPMS) you need to add the `java.scripting` module to the " //
						+ "root modules via `--add-modules ...,java.scripting`";
				return new ThrowingEvaluator(message, cause);
			}
			// Now create the evaluator instance specified by its class name.
			try {
				return (Evaluator) Class.forName(name).getDeclaredConstructor().newInstance();
			}
			catch (ReflectiveOperationException cause) {
				String message = "Creating instance of class `" + name + "` failed," //
						+ "script-based test execution is disabled.";
				return new ThrowingEvaluator(message, cause);
			}
		}

	}

	/**
	 * Evaluator implementation that always throws an {@link ExtensionConfigurationException}.
	 */
	static class ThrowingEvaluator implements Evaluator {

		final ExtensionConfigurationException exception;

		ThrowingEvaluator(String message, Throwable cause) {
			this.exception = new ExtensionConfigurationException(message, cause);
		}

		@Override
		public ConditionEvaluationResult evaluate(ExtensionContext context, List<Script> scripts) {
			throw exception;
		}
	}

}
