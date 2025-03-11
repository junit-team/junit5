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
			for (DiscoveryIssue issue : issues) {
				message.append("\n- [").append(issue.severity()).append("] ").append(issue.message());
				issue.source().ifPresent(s -> message.append(" at ").append(s).append("]"));
				issue.cause().ifPresent(t -> message.append("\n  Caused by: ").append(getStackTrace(t)));
			}
		}
		return message.toString();
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
