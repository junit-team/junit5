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
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_ENVIRONMENT;
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_PROPERTY;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

class ScriptExecutionManager implements CloseableResource {

	private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<>();
	private final ConcurrentMap<Script, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

	private final ScriptAccessor systemPropertyAccessor = new ScriptAccessor.SystemPropertyAccessor();
	private final ScriptAccessor environmentVariableAccessor = new ScriptAccessor.EnvironmentVariableAccessor();

	@Override
	public void close() {
		compiledScripts.clear();
		scriptEngines.clear();
	}

	ConditionEvaluationResult evaluate(Script script, Bindings bindings) throws ScriptException {
		CompiledScript compiledScript = compiledScripts.get(script);

		if (compiledScript == null) {
			String source = script.getSource();
			ScriptEngine scriptEngine = scriptEngines.computeIfAbsent(script.getEngine(), this::createScriptEngine);
			if (!(scriptEngine instanceof Compilable)) {
				return computeConditionEvaluationResult(script, scriptEngine.eval(source, bindings));
			}
			compiledScript = ((Compilable) scriptEngine).compile(source);
			compiledScripts.putIfAbsent(script, compiledScript);
		}

		return computeConditionEvaluationResult(script, compiledScript.eval(bindings));
	}

	ConditionEvaluationResult computeConditionEvaluationResult(Script script, Object result) {
		// Trivial case: script returned a custom ConditionEvaluationResult instance.
		if (result instanceof ConditionEvaluationResult) {
			return (ConditionEvaluationResult) result;
		}

		String resultAsString = String.valueOf(result);
		boolean isTrue;

		if (result instanceof Boolean) {
			isTrue = (Boolean) result;
		}
		else {
			isTrue = Boolean.parseBoolean(resultAsString);
		}

		String reason = script.toReasonString(resultAsString);
		if (script.getAnnotationType() == EnabledIf.class) {
			return isTrue ? enabled(reason) : disabled(reason);
		}
		if (script.getAnnotationType() == DisabledIf.class) {
			return isTrue ? disabled(reason) : enabled(reason);
		}
		throw new JUnitException("Unsupported annotation type: " + script.getAnnotationType());
	}

	ScriptEngine createScriptEngine(String engine) {
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(engine);
		if (scriptEngine == null) {
			scriptEngine = scriptEngineManager.getEngineByExtension(engine);
		}
		if (scriptEngine == null) {
			scriptEngine = scriptEngineManager.getEngineByMimeType(engine);
		}
		Preconditions.notNull(scriptEngine, () -> "Script engine not found: " + engine);

		Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
		bindings.put(SYSTEM_PROPERTY, systemPropertyAccessor);
		bindings.put(SYSTEM_ENVIRONMENT, environmentVariableAccessor);
		return scriptEngine;
	}

}
