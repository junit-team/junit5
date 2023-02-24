/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.junit.platform.console.options.CommandResult.success;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.stream.StreamSupport;

import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.core.ServiceLoaderTestEngineRegistry;

public class ListTestEnginesCommand implements Command<Void> {

	@Override
	public CommandResult<Void> run(PrintWriter out, PrintWriter err) throws Exception {
		displayEngines(out);
		return success();
	}

	void displayEngines(PrintWriter out) {
		ServiceLoaderTestEngineRegistry registry = new ServiceLoaderTestEngineRegistry();
		Iterable<TestEngine> engines = registry.loadTestEngines();
		StreamSupport.stream(engines.spliterator(), false) //
				.sorted(Comparator.comparing(TestEngine::getId)) //
				.forEach(engine -> displayEngine(out, engine));
	}

	private void displayEngine(PrintWriter out, TestEngine engine) {
		StringJoiner details = new StringJoiner(":", " (", ")");
		engine.getGroupId().ifPresent(details::add);
		engine.getArtifactId().ifPresent(details::add);
		engine.getVersion().ifPresent(details::add);
		out.println(engine.getId() + details);
	}
}
