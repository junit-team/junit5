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

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.DiscoveryIssue;

class DiscoveryIssueException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public static DiscoveryIssueException from(String engineId, List<DiscoveryIssue> issues) {
		String message = String.format("TestEngine with ID '%s' encountered %s during test discovery", engineId,
			issues.size() == 1 ? "an issue" : issues.size() + " issues");
		return new DiscoveryIssueException(message, issues);
	}

	private final transient List<DiscoveryIssue> issues;

	private DiscoveryIssueException(String message, List<DiscoveryIssue> issues) {
		super(message, null, false, false);
		this.issues = new ArrayList<>(issues);
	}

	public List<DiscoveryIssue> getIssues() {
		return issues;
	}
}
