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
import java.util.Optional;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.engine.script.Script;
import org.junit.jupiter.engine.script.ScriptAccessor;
import org.junit.jupiter.engine.script.ScriptExecutionManager;
import org.junit.platform.commons.JUnitException;

/**
 * {@link ExecutionCondition} that supports the {@link DisabledIf} and {@link EnabledIf} annotation.
 *
 * @since 5.1
 * @see DisabledIf
 * @see EnabledIf
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
class ScriptExecutionCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled(
		"@DisabledIf and @EnabledIf not present");

	private static final Namespace NAMESPACE = Namespace.create(ScriptExecutionCondition.class);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		// Context without an annotated element?
		Optional<AnnotatedElement> element = context.getElement();
		if (!element.isPresent()) {
			return ENABLED_BY_DEFAULT;
		}
		AnnotatedElement annotatedElement = element.get();

		// Always try to create script instances.
		Script disabledIfScript = createDisabledIfScript(annotatedElement);
		Script enabledIfScript = createEnabledIfScript(annotatedElement);

		// If both scripts are null, no annotation of interest is attached to the underlying element.
		if (disabledIfScript == null && enabledIfScript == null) {
			return ENABLED_BY_DEFAULT;
		}

		// Delegate actual script execution to the globally cached manager instance.
		ScriptExecutionManager scriptExecutionManager = getScriptExecutionManager(context);
		Bindings bindings = createBindings(context);
		ConditionEvaluationResult disabledResult = evaluate(scriptExecutionManager, disabledIfScript, bindings);
		ConditionEvaluationResult enabledResult = evaluate(scriptExecutionManager, enabledIfScript, bindings);

		int flag = 0;
		if (disabledResult != null && disabledResult.isDisabled()) {
			flag += 1;
		}
		if (enabledResult != null && enabledResult.isDisabled()) {
			flag += 2;
		}

		switch (flag) {
			case 0:
				return enabled("Nothing's disabled."); // Both are enabled. Combine messages, if possible.
			case 1:
				return disabledResult; // Only disabled is disabled.
			case 2:
				return enabledResult; // Only enabled is disabled.
			case 3:
				return disabled("Both are disabled"); // Both are disabled. Combine messages.
			default:
				throw new JUnitException("Should never happen!");
		}
	}

	private Script createDisabledIfScript(AnnotatedElement annotatedElement) {
		Optional<DisabledIf> disabled = findAnnotation(annotatedElement, DisabledIf.class);
		if (!disabled.isPresent()) {
			return null;
		}
		DisabledIf annotation = disabled.get();
		String source = createSource(annotation.value());
		return new Script(annotation, annotation.engine(), source, annotation.reason());
	}

	private Script createEnabledIfScript(AnnotatedElement annotatedElement) {
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
			throw new JUnitException("Script evaluation failed for: " + script.getAnnotationAsString(), e);
		}
	}

	ConditionEvaluationResult computeConditionEvaluationResult(Script script, Object result) {
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
		throw new JUnitException("Unsupported annotation type: " + script.getAnnotationType());
	}

}
