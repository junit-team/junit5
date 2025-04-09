/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.partitioningBy;
import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * @since 1.13
 */
class DiscoveryIssueNotifier {

	static final DiscoveryIssueNotifier NO_ISSUES = new DiscoveryIssueNotifier(emptyList(), emptyList(), emptyList());
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryIssueNotifier.class);

	private final List<DiscoveryIssue> allIssues;
	private final List<DiscoveryIssue> criticalIssues;
	private final List<DiscoveryIssue> nonCriticalIssues;

	static DiscoveryIssueNotifier from(Severity criticalSeverity, List<DiscoveryIssue> issues) {
		Map<Boolean, List<DiscoveryIssue>> issuesByCriticality = issues.stream() //
				.sorted(comparing(DiscoveryIssue::severity).reversed()) //
				.collect(partitioningBy(issue -> issue.severity().compareTo(criticalSeverity) >= 0));
		List<DiscoveryIssue> criticalIssues = issuesByCriticality.get(true);
		List<DiscoveryIssue> nonCriticalIssues = issuesByCriticality.get(false);
		return new DiscoveryIssueNotifier(new ArrayList<>(issues), criticalIssues, nonCriticalIssues);
	}

	private DiscoveryIssueNotifier(List<DiscoveryIssue> allIssues, List<DiscoveryIssue> criticalIssues,
			List<DiscoveryIssue> nonCriticalIssues) {
		this.allIssues = allIssues;
		this.criticalIssues = criticalIssues;
		this.nonCriticalIssues = nonCriticalIssues;
	}

	List<DiscoveryIssue> getAllIssues() {
		return allIssues;
	}

	boolean hasCriticalIssues() {
		return !criticalIssues.isEmpty();
	}

	void logCriticalIssues(TestEngine testEngine) {
		logIssues(testEngine, criticalIssues, "critical");
	}

	void logNonCriticalIssues(TestEngine testEngine) {
		logIssues(testEngine, nonCriticalIssues, "non-critical");
	}

	DiscoveryIssueException createExceptionForCriticalIssues(TestEngine testEngine) {
		if (criticalIssues.isEmpty()) {
			return null;
		}
		String message = formatMessage(testEngine, criticalIssues, "critical");
		return new DiscoveryIssueException(message);
	}

	private void logIssues(TestEngine testEngine, List<DiscoveryIssue> issues, String adjective) {
		if (!issues.isEmpty()) {
			Severity maxSeverity = issues.get(0).severity();
			logger(maxSeverity).accept(() -> formatMessage(testEngine, issues, adjective));
		}
	}

	private static Consumer<Supplier<String>> logger(Severity severity) {
		// TODO [#4246] Use switch expression
		switch (severity) {
			case INFO:
				return logger::info;
			case WARNING:
				return logger::warn;
			case ERROR:
				return logger::error;
			default:
				throw new IllegalArgumentException("Unknown severity: " + severity);
		}
	}

	private static String formatMessage(TestEngine testEngine, List<DiscoveryIssue> issues, String adjective) {
		Preconditions.notNull(testEngine, "testEngine must not be null");
		Preconditions.notNull(issues, "issues must not be null");
		Preconditions.notEmpty(issues, "issues must not be empty");
		String engineId = testEngine.getId();
		StringBuilder message = new StringBuilder();
		message.append("TestEngine with ID '").append(engineId).append("' encountered ");
		if (issues.size() == 1) {
			message.append("a ").append(adjective).append(" issue");
		}
		else {
			message.append(issues.size()).append(' ').append(adjective).append(" issues");
		}
		message.append(" during test discovery:");
		for (int i = 0; i < issues.size(); i++) {
			DiscoveryIssue issue = issues.get(i);
			message.append("\n\n(").append(i + 1).append(") [").append(issue.severity()).append("] ").append(
				issue.message());
			issue.source().ifPresent(source -> {
				message.append("\n    Source: ").append(source);
				if (source instanceof MethodSource) {
					MethodSource methodSource = (MethodSource) source;
					appendIdeCompatibleLink(message, methodSource.getClassName(), methodSource.getMethodName());
				}
				else if (source instanceof ClassSource) {
					ClassSource classSource = (ClassSource) source;
					appendIdeCompatibleLink(message, classSource.getClassName(), "<no-method>");
				}
			});
			issue.cause().ifPresent(t -> message.append("\n    Cause: ").append(readStackTrace(t)));
		}
		return message.toString();
	}

	private static void appendIdeCompatibleLink(StringBuilder message, String className, String methodName) {
		message.append("\n            at ").append(className).append(".").append(methodName).append("(SourceFile:0)");
	}
}
