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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@code DiscoveryIssueException} is an exception that is thrown if an engine
 * reports critical issues during test discovery.
 *
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public class DiscoveryIssueException extends JUnitException {

	private static final long serialVersionUID = 1L;

	static DiscoveryIssueException from(String engineId, List<DiscoveryIssue> issues) {
		return new DiscoveryIssueException(formatMessageForCriticalIssues(engineId, issues));
	}

	static String formatMessageForNonCriticalIssues(String engineId, List<DiscoveryIssue> issues) {
		return formatMessage(engineId, issues, "non-critical");
	}

	private static String formatMessageForCriticalIssues(String engineId, List<DiscoveryIssue> issues) {
		return formatMessage(engineId, issues, "critical");
	}

	private static String formatMessage(String engineId, List<DiscoveryIssue> issues, String adjective) {
		Preconditions.notNull(engineId, "engineId must not be null");
		Preconditions.notNull(issues, "issues must not be null");
		Preconditions.notEmpty(issues, "issues must not be empty");
		StringBuilder message = new StringBuilder(
			String.format("TestEngine with ID '%s' encountered %s during test discovery", engineId,
				issues.size() == 1 ? String.format("a %s issue", adjective)
						: String.format("%d %s issues", issues.size(), adjective)));
		message.append(":");
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
			issue.cause().ifPresent(t -> message.append("\n    Cause: ").append(getStackTrace(t)));
		}
		return message.toString();
	}

	private static void appendIdeCompatibleLink(StringBuilder message, String className, String methodName) {
		message.append("\n            at ").append(className).append(".").append(methodName).append("(SourceFile:0)");
	}

	private static String getStackTrace(Throwable cause) {
		StringWriter stringWriter = new StringWriter();
		try (PrintWriter writer = new PrintWriter(stringWriter, true)) {
			cause.printStackTrace(writer);
			writer.flush();
		}
		return stringWriter.toString();
	}

	private DiscoveryIssueException(String message) {
		super(message, null, false, false);
	}
}
