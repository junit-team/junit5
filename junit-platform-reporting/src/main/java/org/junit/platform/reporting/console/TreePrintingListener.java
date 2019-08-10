/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.console;

import static org.junit.platform.reporting.console.Theme.ASCII;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Listener that prints output to the provided {@link PrintWriter} instance. The
 * output is flat rather than hierarchical.
 * <p>
 *
 * Optionally will use ANSI escape codes to colorize the output.
 * <p>
 *
 * Can be configured to use plain ASCII or Unicode characters for the tree
 * elements.
 *
 * Note that this listener buffers the results and prints them all at the end
 * when {@link #testPlanExecutionFinished(TestPlan) testPlanExecutionFinished()}
 * is called. If you want to see the test results printed as they execute, try
 * the {@link VerboseTreePrintingListener}.
 *
 * @since 1.0
 * @see FlatPrintingListener
 * @see VerboseTreePrintingListener
 */
public class TreePrintingListener implements TestExecutionListener {

	private final Map<String, TreeNode> nodesByUniqueId = new ConcurrentHashMap<>();
	private TreeNode root;
	private final TreePrinter treePrinter;

	/**
	 * Creates a new listener that prints hierarchical output to {@code System.out}
	 * using a monochromatic {@link Theme#ASCII ASCII} theme.
	 */
	public TreePrintingListener() {
		this(new PrintWriter(System.out));
	}

	/**
	 * Creates a new listener that prints hierarchical output to the given printer
	 * using a monochromatic {@link Theme#ASCII ASCII} theme.
	 *
	 * @param out the printer to which the listener will print.
	 */
	public TreePrintingListener(PrintWriter out) {
		this(out, false, ASCII);
	}

	/**
	 * Creates a new listener that prints hierarchical output to the given printer.
	 *
	 * @param out           the printer to which the listener will print.
	 * @param useAnsiColors {@code true} to use ANSI color codes to colorize the
	 *                      output, {@code false} to use monochromatic output.
	 * @param theme         the style to use when printing the hierarchy (see
	 *                      {@link Theme}).
	 */
	public TreePrintingListener(PrintWriter out, boolean useAnsiColors, Theme theme) {
		this.treePrinter = new TreePrinter(out, theme, useAnsiColors);
	}

	private TreeNode addNode(TestIdentifier testIdentifier, Supplier<TreeNode> nodeSupplier) {
		TreeNode node = nodeSupplier.get();
		nodesByUniqueId.put(testIdentifier.getUniqueId(), node);
		testIdentifier.getParentId().map(nodesByUniqueId::get).orElse(root).addChild(node);
		return node;
	}

	private TreeNode getNode(TestIdentifier testIdentifier) {
		return nodesByUniqueId.get(testIdentifier.getUniqueId());
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
		addNode(testIdentifier, () -> new TreeNode(testIdentifier));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		getNode(testIdentifier).setResult(testExecutionResult);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		addNode(testIdentifier, () -> new TreeNode(testIdentifier, reason));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		getNode(testIdentifier).addReportEntry(entry);
	}

}
