/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.script;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * Entry point for script execution support.
 *
 * @since 5.1
 */
@API(status = INTERNAL, since = "5.1")
@Deprecated
public class ScriptExecutionManager {

	private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	private final ConcurrentMap<String, ScriptEngine> scriptEngines = new ConcurrentHashMap<>();
	private final ConcurrentMap<Script, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

	private final ScriptAccessor systemPropertyAccessor = new ScriptAccessor.SystemPropertyAccessor();
	private final ScriptAccessor environmentVariableAccessor = new ScriptAccessor.EnvironmentVariableAccessor();

	// package-private for testing purposes -- make it configurable?
	boolean forceScriptEvaluation = false;

	/**
	 * Evaluate the script using the given bindings.
	 *
	 * @param script the script to evaluate
	 * @param bindings the context-aware bindings
	 * @return the result object
	 * @throws ScriptException if an error occurs in script.
	 */
	public Object evaluate(Script script, Bindings bindings) throws ScriptException {
		// Always look for a compiled script in our cache.
		CompiledScript compiledScript = compiledScripts.get(script);

		// No compiled script found?
		if (compiledScript == null) {
			String source = script.getSource();
			ScriptEngine scriptEngine = scriptEngines.computeIfAbsent(script.getEngine(), this::createScriptEngine);
			if (!(scriptEngine instanceof Compilable) || forceScriptEvaluation) {
				return scriptEngine.eval(source, bindings);
			}
			// Compile and store it in our cache. Fall-through for execution
			compiledScript = ((Compilable) scriptEngine).compile(source);
			compiledScripts.putIfAbsent(script, compiledScript);
		}

		// Let the cached compiled script do its work.
		return compiledScript.eval(bindings);
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
		bindings.put(Script.BIND_SYSTEM_PROPERTY, systemPropertyAccessor);
		bindings.put(Script.BIND_SYSTEM_ENVIRONMENT, environmentVariableAccessor);
		return scriptEngine;
	}

	boolean isCompiledScriptsEmpty() {
		return compiledScripts.isEmpty();
	}
}
