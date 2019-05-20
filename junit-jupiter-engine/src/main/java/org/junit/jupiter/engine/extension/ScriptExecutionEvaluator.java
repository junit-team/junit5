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

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ScriptEvaluationException;
import org.junit.jupiter.engine.script.Script;
import org.junit.jupiter.engine.script.ScriptAccessor;
import org.junit.jupiter.engine.script.ScriptExecutionManager;

/**
 * Encapsulates javax.script-related evaluation work.
 *
 * <p>This class is instantiated via reflection in class {@link ScriptExecutionCondition}.
 *
 * @since 5.1
 */
@Deprecated
class ScriptExecutionEvaluator implements ScriptExecutionCondition.Evaluator {

	private static final ConditionEvaluationResult ENABLED_ALL = enabled("All results are enabled");

	private final ScriptExecutionManager scriptExecutionManager = new ScriptExecutionManager();

	@Override
	public ConditionEvaluationResult evaluate(ExtensionContext context, List<Script> scripts) {
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
