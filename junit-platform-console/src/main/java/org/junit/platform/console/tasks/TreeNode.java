/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
class TreeNode {

	private final String caption;
	private final long creation;
	long duration;
	private String reason;
	private TestIdentifier identifier;
	private TestExecutionResult result;
	List<ReportEntry> reports = Collections.emptyList();
	List<TreeNode> children = Collections.emptyList();
	boolean visible;

	TreeNode(String caption) {
		this.caption = caption;
		this.creation = System.currentTimeMillis();
		this.visible = false;
	}

	TreeNode(TestIdentifier identifier) {
		this(identifier.getDisplayName());
		this.identifier = identifier;
		this.visible = true;
	}

	TreeNode(TestIdentifier identifier, String reason) {
		this(identifier);
		this.reason = reason;
	}

	TreeNode addChild(TreeNode node) {
		if (children == Collections.EMPTY_LIST) {
			children = new ArrayList<>();
		}
		children.add(node);
		return this;
	}

	TreeNode addReportEntry(ReportEntry reportEntry) {
		if (reports == Collections.EMPTY_LIST) {
			reports = new ArrayList<>();
		}
		reports.add(reportEntry);
		return this;
	}

	TreeNode setResult(TestExecutionResult result) {
		this.result = result;
		this.duration = System.currentTimeMillis() - creation;
		return this;
	}

	public String caption() {
		return caption;
	}

	Optional<String> reason() {
		return Optional.ofNullable(reason);
	}

	Optional<TestExecutionResult> result() {
		return Optional.ofNullable(result);
	}

	Optional<TestIdentifier> identifier() {
		return Optional.ofNullable(identifier);
	}
}
