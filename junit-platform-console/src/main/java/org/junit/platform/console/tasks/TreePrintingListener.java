/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TreePrintingListener implements DetailsPrintingListener {

	private final Map<UniqueId, TreeNode> nodesByUniqueId = new ConcurrentHashMap<>();
	private TreeNode root;
	private final TreePrinter treePrinter;

	TreePrintingListener(PrintWriter out, ColorPalette colorPalette, Theme theme) {
		this.treePrinter = new TreePrinter(out, theme, colorPalette);
	}

	private void addNode(TestIdentifier testIdentifier, TreeNode node) {
		nodesByUniqueId.put(testIdentifier.getUniqueIdObject(), node);
		testIdentifier.getParentIdObject().map(nodesByUniqueId::get).orElse(root).addChild(node);
	}

	private TreeNode getNode(TestIdentifier testIdentifier) {
		return nodesByUniqueId.get(testIdentifier.getUniqueIdObject());
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		root = new TreeNode(testPlan.toString());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		treePrinter.print(root);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		addNode(testIdentifier, new TreeNode(testIdentifier));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		getNode(testIdentifier).setResult(testExecutionResult);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		addNode(testIdentifier, new TreeNode(testIdentifier, reason));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		getNode(testIdentifier).addReportEntry(entry);
	}

	@Override
	public void listTests(TestPlan testPlan) {
		root = new TreeNode(testPlan.toString());
		testPlan.accept(new TestPlan.Visitor() {
			@Override
			public void visit(TestIdentifier testIdentifier) {
				addNode(testIdentifier, new TreeNode(testIdentifier));
			}
		});
		treePrinter.print(root);
	}
}
