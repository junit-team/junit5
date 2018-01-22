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
import static org.junit.jupiter.engine.Constants.DEFAULT_ENABLED_IF_SCRIPT_ENGINE_PROPERTY_NAME;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
		String engine = annotation.engine();
		if (engine.isEmpty()) {
			engine = context.getConfigurationParameter(DEFAULT_ENABLED_IF_SCRIPT_ENGINE_PROPERTY_NAME) //
					.orElse("javascript");
		}
		ScriptEngine scriptEngine = findScriptEngine(engine);
		logger.debug(() -> "ScriptEngine: " + scriptEngine);

		// Prepare bindings
		Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		if (!annotation.bindExtensionContext().isEmpty()) {
			bindings.put(annotation.bindExtensionContext(), context);
		}
		if (!annotation.bindSystemProperties().isEmpty()) {
			bindings.put(annotation.bindSystemProperties(), System.getProperties());
		}
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
		boolean ok = Boolean.parseBoolean(String.valueOf(result));
		String reason = annotation.reason();
		if (reason.isEmpty()) {
			reason = String.format("Script `%s` evaluated to: %s", script, result);
		}
		return ok ? enabled(reason) : disabled(reason);
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
		// trivial case: no imports, single script line
		if (annotation.imports().length == 0 && annotation.value().length == 1) {
			return annotation.value()[0];
		}

		switch (language) {
			case "ECMAScript":
				return createJavaScript(annotation);
			case "Groovy":
				return createGroovyScript(annotation);
			default:
				return joinLines(annotation.delimiter(), Arrays.asList(annotation.value()));
		}
	}

	private String createJavaScript(EnabledIf annotation) {
		boolean injectJavaImporterStatements = annotation.imports().length > 0;
		List<String> lines = new ArrayList<>();
		if (injectJavaImporterStatements) {
			String imports = String.join(", ", annotation.imports());
			lines.add("var javaImporter = new JavaImporter(" + imports + ")");
			lines.add("with (javaImporter) {");
		}
		for (String line : annotation.value()) {
			if (injectJavaImporterStatements) {
				line = "  " + line;
			}
			lines.add(line);
		}
		if (injectJavaImporterStatements) {
			lines.add("}");
			lines.add("");
		}
		return joinLines(annotation.delimiter(), lines);
	}

	private String createGroovyScript(EnabledIf annotation) {
		List<String> lines = new ArrayList<>();
		for (String importLine : annotation.imports()) {
			importLine = "import " + importLine;
			lines.add(importLine);
		}
		if (!lines.isEmpty()) {
			lines.add("");
		}
		lines.addAll(Arrays.asList(annotation.value()));
		return joinLines(annotation.delimiter(), lines);
	}

	private String joinLines(String delimiter, Iterable<? extends CharSequence> elements) {
		if (delimiter.isEmpty()) {
			delimiter = System.lineSeparator();
		}
		return String.join(delimiter, elements);
	}

}
