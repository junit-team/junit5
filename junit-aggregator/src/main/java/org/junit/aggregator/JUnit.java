/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModule;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.function.UnaryOperator;
import java.util.spi.ToolProvider;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

@API(status = EXPERIMENTAL, since = "6.0")
public final class JUnit {

	private JUnit() {
	}

	public static void run(Object instance) {
		if (instance instanceof Class<?> testClass) {
			run(testClass);
			return;
		}
		if (instance instanceof Module testModule) {
			run(testModule);
			return;
		}
		run(instance.getClass());
	}

	public static void run(Class<?> testClass) {
		run(discovery -> discovery.selectors(selectClass(testClass)));
	}

	public static void run(Module testModule) {
		run(discovery -> discovery.selectors(selectModule(testModule.getName())));
	}

	public static void run(UnaryOperator<LauncherDiscoveryRequestBuilder> discovery) {
		var listener = new SummaryGeneratingListener();
		var request = discovery.apply(request()).forExecution().listeners(listener).build();
		LauncherFactory.create().execute(request);
		var summary = listener.getSummary();
		summary.printTo(new PrintWriter(System.out, true, Charset.defaultCharset()));
		if (!summary.getFailures().isEmpty()) {
			summary.printFailuresTo(new PrintWriter(System.err, true, Charset.defaultCharset()));
			throw new JUnitException("There are test failures!");
		}
	}

	public static void main(String[] args) {
		var junit = ToolProvider.findFirst("junit").orElseThrow();
		var exitCode = junit.run(System.out, System.err, args);
		System.exit(exitCode);
	}
}
