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
		// Always try to create a script instance for the current context.
		Script script = createScript(context);

		// If the script is null, no annotation of interest is attached to the underlying element.
		if (script == null) {
			return ENABLED_BY_DEFAULT;
		}

		// Delegate actual script execution and result evaluation to the globally cached manager instance.
		ScriptExecutionManager scriptExecutionManager = getScriptExecutionManager(context);
		try {
			Bindings bindings = createBindings(context);
			return scriptExecutionManager.evaluate(script, bindings);
		}
		catch (ScriptException e) {
			throw new JUnitException("Script evaluation failed for: " + script.getAnnotationAsString(), e);
		}
	}

	private Script createScript(ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		if (!element.isPresent()) {
			return null;
		}
		AnnotatedElement annotatedElement = element.get();

		Optional<DisabledIf> disabled = findAnnotation(annotatedElement, DisabledIf.class);
		if (disabled.isPresent()) {
			DisabledIf annotation = disabled.get();
			String source = createSource(annotation.value());
			return new Script(annotation, annotation.engine(), source, annotation.reason());
		}

		Optional<EnabledIf> enabled = findAnnotation(annotatedElement, EnabledIf.class);
		if (enabled.isPresent()) {
			EnabledIf annotation = enabled.get();
			String source = createSource(annotation.value());
			return new Script(annotation, annotation.engine(), source, annotation.reason());
		}

		return null;
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

}
