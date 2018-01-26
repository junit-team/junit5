/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} that supports the {@link EnabledIf @EnabledIf} annotation.
 *
 * @since 5.1
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
class EnabledIfCondition implements ExecutionCondition {

	private static final String DEFAULT_SCRIPT_ENGINE_NAME = "Nashorn";
	private static final Logger logger = LoggerFactory.getLogger(EnabledIfCondition.class);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		Optional<EnabledIf> optionalAnnotation = findAnnotation(element, EnabledIf.class);
		if (!optionalAnnotation.isPresent()) {
			return enabled("@EnabledIf is not present");
		}

		EnabledIf annotation = optionalAnnotation.get();
		Preconditions.notEmpty(annotation.value(), "String[] returned by @EnabledIf.value() must not be empty");

		// Find script engine
		ScriptEngine scriptEngine = findScriptEngine(DEFAULT_SCRIPT_ENGINE_NAME);
		logger.debug(() -> "ScriptEngine: " + scriptEngine);

		// Prepare bindings
		Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("systemProperty", PropertyAccessor.of(System::getProperty));
		bindings.put("systemEnvironment", PropertyAccessor.of(System::getenv));
		bindings.put("jupiterConfigurationParameter", PropertyAccessor.of(context::getConfigurationParameter));
		logger.debug(() -> "Bindings: " + bindings);

		// Build actual script text from annotation properties
		String script = createScript(annotation, scriptEngine.getFactory().getLanguageName());
		logger.debug(() -> "Script: " + script);

		return evaluate(annotation, scriptEngine, script);
	}

	private ConditionEvaluationResult evaluate(EnabledIf annotation, ScriptEngine scriptEngine, String script) {
		Object result;
		try {
			result = scriptEngine.eval(script);
		}
		catch (ScriptException e) {
			logger.warn(e, () -> "Evaluation of @EnabledIf script failed, disabling execution");
			return disabled(e.toString());
		}

		// Trivial case: script returned a custom ConditionEvaluationResult instance.
		if (result instanceof ConditionEvaluationResult) {
			return (ConditionEvaluationResult) result;
		}

		// Parse result for "true" (ignoring case) and prepare reason message.
		String resultAsString = String.valueOf(result);
		String reason = createReason(annotation, script, resultAsString);
		return Boolean.parseBoolean(resultAsString) ? enabled(reason) : disabled(reason);
	}

	ScriptEngine findScriptEngine(String string) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine scriptEngine = manager.getEngineByName(string);
		if (scriptEngine == null) {
			scriptEngine = manager.getEngineByExtension(string);
		}
		if (scriptEngine == null) {
			scriptEngine = manager.getEngineByMimeType(string);
		}
		Preconditions.notNull(scriptEngine, "Script engine not found: " + string);
		return scriptEngine;
	}

	String createScript(EnabledIf annotation, String language) {
		// trivial case: one liner
		if (annotation.value().length == 1) {
			return annotation.value()[0];
		}

		return joinLines(System.lineSeparator(), Arrays.asList(annotation.value()));
	}

	String createReason(EnabledIf annotation, String script, String result) {
		String reason = annotation.reason();
		reason = reason.replace("{{script}}", script);
		reason = reason.replace("{{result}}", result);
		return reason;
	}

	private String joinLines(String delimiter, Iterable<? extends CharSequence> elements) {
		if (delimiter.isEmpty()) {
			delimiter = System.lineSeparator();
		}
		return String.join(delimiter, elements);
	}

	/**
	 * Provides read-only access to e.g. values of {@link java.util.Map}.
	 *
	 * <p>Usage example: {@code PropertyAccessor.of(System::getProperty)} is
	 * analog to writing {@code System.getProperty(key)}.
	 */
	public static class PropertyAccessor {

		static PropertyAccessor of(Function<String, Object> accessor) {
			return new PropertyAccessor(accessor);
		}

		private final Function<String, Object> accessor;

		private PropertyAccessor(Function<String, Object> accessor) {
			this.accessor = accessor;
		}

		public String get(String key) {
			Object value = accessor.apply(key);
			if (value instanceof Optional) {
				value = ((Optional<?>) value).orElse(null);
			}
			return String.valueOf(value);
		}
	}
}
