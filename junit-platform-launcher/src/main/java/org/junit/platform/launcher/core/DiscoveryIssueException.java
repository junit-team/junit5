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

import static java.util.Comparator.comparing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

class DiscoveryIssueException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public static DiscoveryIssueException from(String engineId, List<DiscoveryIssue> issues) {
		Preconditions.notNull(engineId, "engineId must not be null");
		Preconditions.notNull(issues, "issues must not be null");
		Preconditions.notEmpty(issues, "issues must not be empty");
		String message = String.format("TestEngine with ID '%s' encountered %s during test discovery", engineId,
			issues.size() == 1 ? "an issue" : issues.size() + " issues");
		return new DiscoveryIssueException(message, issues);
	}

	private final transient List<DiscoveryIssue> issues;

	private DiscoveryIssueException(String message, List<DiscoveryIssue> issues) {
		super(message, null, false, false);
		this.issues = new ArrayList<>(issues);
		this.issues.sort(comparing(DiscoveryIssue::severity).reversed());
	}

	@Override
	public String getMessage() {
		StringBuilder message = new StringBuilder(super.getMessage());
		if (issues != null) { // after deserialization
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
}
