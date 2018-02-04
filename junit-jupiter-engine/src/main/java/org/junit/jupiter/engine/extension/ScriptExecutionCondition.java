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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ScriptEvaluationException;
import org.junit.jupiter.engine.script.Script;
import org.junit.jupiter.engine.script.ScriptAccessor;
import org.junit.jupiter.engine.script.ScriptExecutionManager;

/**
 * {@link ExecutionCondition} that supports the {@link DisabledIf} and {@link EnabledIf} annotation.
 *
 * @since 5.1
 * @see DisabledIf
 * @see EnabledIf
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
class ScriptExecutionCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_NO_ELEMENT = enabled("AnnotatedElement not present");

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled("Annotation not present");

	private static final ConditionEvaluationResult ENABLED_ALL = enabled("All results are enabled");

	private static final Namespace NAMESPACE = Namespace.create(ScriptExecutionCondition.class);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		// Context without an annotated element?
		Optional<AnnotatedElement> element = context.getElement();
		if (!element.isPresent()) {
			return ENABLED_NO_ELEMENT;
		}
		AnnotatedElement annotatedElement = element.get();

		// Always try to create script instances.
		List<Script> scripts = new ArrayList<>();
		createDisabledIfScript(annotatedElement).ifPresent(scripts::add);
		createEnabledIfScript(annotatedElement).ifPresent(scripts::add);

		// If no scripts are created, no annotation of interest is attached to the underlying element.
		if (scripts.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		// Delegate actual script execution to the globally cached manager instance.
		ScriptExecutionManager scriptExecutionManager = getScriptExecutionManager(context);
		Bindings bindings = createBindings(context);
		for (Script script : scripts) {
			ConditionEvaluationResult result = evaluate(scriptExecutionManager, script, bindings);
			// Report the first result that is disabled, preventing evaluation of remaining scripts.
			if (result.isDisabled()) {
				return result;
			}
		}

		return ENABLED_ALL;
	}

	private Optional<Script> createDisabledIfScript(AnnotatedElement annotatedElement) {
		Optional<DisabledIf> disabled = findAnnotation(annotatedElement, DisabledIf.class);
		if (!disabled.isPresent()) {
			return Optional.empty();
		}
		DisabledIf annotation = disabled.get();
		String source = createSource(annotation.value());
		Script script = new Script(annotation, annotation.engine(), source, annotation.reason());
		return Optional.of(script);
	}

	private Optional<Script> createEnabledIfScript(AnnotatedElement annotatedElement) {
		Optional<EnabledIf> enabled = findAnnotation(annotatedElement, EnabledIf.class);
		if (!enabled.isPresent()) {
			return Optional.empty();
		}
		EnabledIf annotation = enabled.get();
		String source = createSource(annotation.value());
		Script script = new Script(annotation, annotation.engine(), source, annotation.reason());
		return Optional.of(script);
	}

	private String createSource(String[] lines) {
		return String.join(System.lineSeparator(), lines);
	}

	private ScriptExecutionManager getScriptExecutionManager(ExtensionContext context) {
		return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(ScriptExecutionManager.class);
	}

	private Bindings createBindings(ExtensionContext context) {
		ScriptAccessor configurationParameterAccessor = new ScriptAccessor.ConfigurationParameterAccessor(context);
		Bindings bindings = new SimpleBindings();
		bindings.put(Script.BIND_JUNIT_TAGS, context.getTags());
		bindings.put(Script.BIND_JUNIT_UNIQUE_ID, context.getUniqueId());
		bindings.put(Script.BIND_JUNIT_DISPLAY_NAME, context.getDisplayName());
		bindings.put(Script.BIND_JUNIT_CONFIGURATION_PARAMETER, configurationParameterAccessor);
		return bindings;
	}

	ConditionEvaluationResult evaluate(ScriptExecutionManager manager, Script script, Bindings bindings) {
		if (script == null) {
			return null;
		}
		try {
			Object result = manager.evaluate(script, bindings);
			return computeConditionEvaluationResult(script, result);
		}
		catch (ScriptException e) {
			throw new ScriptEvaluationException("Script evaluation failed for: " + script.getAnnotationAsString(), e);
		}
	}

	ConditionEvaluationResult computeConditionEvaluationResult(Script script, Object result) {
		// Treat "null" result as an error.
		if (result == null) {
			throw new ScriptEvaluationException("Script returned `null`: " + script.getAnnotationAsString());
		}

		// Trivial case: script returned a custom ConditionEvaluationResult instance.
		if (result instanceof ConditionEvaluationResult) {
			return (ConditionEvaluationResult) result;
		}

		String resultAsString = String.valueOf(result);
		String reason = script.toReasonString(resultAsString);

		// Cast or parse result to a boolean value.
		boolean isTrue;
		if (result instanceof Boolean) {
			isTrue = (Boolean) result;
		}
		else {
			isTrue = Boolean.parseBoolean(resultAsString);
		}

		// Flip enabled/disabled result based on the associated annotation type.
		if (script.getAnnotationType() == EnabledIf.class) {
			return isTrue ? enabled(reason) : disabled(reason);
		}
		if (script.getAnnotationType() == DisabledIf.class) {
			return isTrue ? disabled(reason) : enabled(reason);
		}

		// Still here? Not so good.
		throw new ScriptEvaluationException("Unsupported annotation type: " + script.getAnnotationType());
	}

}
