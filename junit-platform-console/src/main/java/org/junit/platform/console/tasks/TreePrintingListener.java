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

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TreePrintingListener implements TestExecutionListener {

	private final Deque<TreeNode> stack;
	private final TreePrinter treePrinter;

	TreePrintingListener(PrintWriter out, boolean disableAnsiColors, Theme theme) {
		this.treePrinter = new TreePrinter(out, theme, disableAnsiColors);
		this.stack = new ArrayDeque<>();
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		stack.push(new TreeNode(testPlan.toString()));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		treePrinter.print(stack.pop());
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		TreeNode node = new TreeNode(testIdentifier);
		stack.peek().addChild(node);
		stack.push(node);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		stack.pop().setResult(testExecutionResult);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		stack.peek().addChild(new TreeNode(testIdentifier, reason));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		stack.peek().addReportEntry(entry);
	}

}
