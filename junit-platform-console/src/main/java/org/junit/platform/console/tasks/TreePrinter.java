/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.console.tasks.Color.CONTAINER;
import static org.junit.platform.console.tasks.Color.FAILED;
import static org.junit.platform.console.tasks.Color.GREEN;
import static org.junit.platform.console.tasks.Color.NONE;
import static org.junit.platform.console.tasks.Color.SKIPPED;
import static org.junit.platform.console.tasks.Color.YELLOW;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * @since 1.0
 */
class TreePrinter {

	private final PrintWriter out;
	private final Theme theme;
	private final boolean disableAnsiColors;

	TreePrinter(PrintWriter out, Theme theme, boolean disableAnsiColors) {
		this.out = out;
		this.theme = theme;
		this.disableAnsiColors = disableAnsiColors;
	}

	void print(TreeNode node) {
		out.println(color(CONTAINER, theme.root()));
		print(node, "", true);
		out.flush();
	}

	private void print(TreeNode node, String indent, boolean continuous) {
		if (node.visible) {
			printVisible(node, indent, continuous);
		}
		if (node.children.isEmpty()) {
			return;
		}
		if (node.visible) {
			indent += continuous ? theme.vertical() : theme.blank();
		}
		Iterator<TreeNode> iterator = node.children.iterator();
		while (iterator.hasNext()) {
			print(iterator.next(), indent, iterator.hasNext());
		}
	}

	private void printVisible(TreeNode node, String indent, boolean continuous) {
		String bullet = continuous ? theme.entry() : theme.end();
		String prefix = color(CONTAINER, indent + bullet);
		String tabbed = color(CONTAINER, indent + tab(node, continuous));
		String caption = colorCaption(node);
		String duration = color(CONTAINER, node.duration + " ms");
		String icon = color(SKIPPED, theme.skipped());
		if (node.result().isPresent()) {
			TestExecutionResult result = node.result().get();
			Color resultColor = Color.valueOf(result);
			icon = color(resultColor, theme.status(result));
		}
		out.print(prefix);
		out.print(" ");
		out.print(caption);
		if (node.duration > 10000 && node.children.isEmpty()) {
			out.print(" ");
			out.print(duration);
		}
		out.print(" ");
		out.print(icon);
		node.result().ifPresent(result -> printThrowable(tabbed, result));
		node.reason().ifPresent(reason -> printMessage(SKIPPED, tabbed, reason));
		node.reports.forEach(e -> printReportEntry(tabbed, e));
		out.println();
	}

	private String tab(TreeNode node, boolean continuous) {
		// We might be the "last" node in this level, that means
		// `continuous == false`, but still need to include a vertical
		// bar for printing stack traces, messages and reports.
		// See https://github.com/junit-team/junit5/issues/1531
		if (node.children.size() > 0) {
			return theme.blank() + theme.vertical();
		}
		return (continuous ? theme.vertical() : theme.blank()) + theme.blank();
	}

	private String colorCaption(TreeNode node) {
		String caption = node.caption();
		if (node.result().isPresent()) {
			TestExecutionResult result = node.result().get();
			Color resultColor = Color.valueOf(result);
			if (result.getStatus() != Status.SUCCESSFUL) {
				return color(resultColor, caption);
			}
		}
		if (node.reason().isPresent()) {
			return color(SKIPPED, caption);
		}
		Color color = node.identifier().map(Color::valueOf).orElse(Color.NONE);
		return color(color, caption);
	}

	private void printThrowable(String indent, TestExecutionResult result) {
		if (!result.getThrowable().isPresent()) {
			return;
		}
		Throwable throwable = result.getThrowable().get();
		String message = throwable.getMessage();
		if (StringUtils.isBlank(message)) {
			message = throwable.toString();
		}
		printMessage(FAILED, indent, message);
	}

	private void printReportEntry(String indent, ReportEntry reportEntry) {
		out.println();
		out.print(indent);
		out.print(reportEntry.getTimestamp());
		Set<Map.Entry<String, String>> entries = reportEntry.getKeyValuePairs().entrySet();
		if (entries.size() == 1) {
			printReportEntry(" ", getOnlyElement(entries));
			return;
		}
		for (Map.Entry<String, String> entry : entries) {
			out.println();
			printReportEntry(indent + theme.blank(), entry);
		}
	}

	private void printReportEntry(String indent, Map.Entry<String, String> mapEntry) {
		out.print(indent);
		out.print(color(YELLOW, mapEntry.getKey()));
		out.print(" = `");
		out.print(color(GREEN, mapEntry.getValue()));
		out.print("`");
	}

	private void printMessage(Color color, String indent, String message) {
		String[] lines = message.split("\\R");
		out.print(" ");
		out.print(color(color, lines[0]));
		if (lines.length > 1) {
			for (int i = 1; i < lines.length; i++) {
				out.println();
				out.print(indent);
				if (StringUtils.isNotBlank(lines[i])) {
					String extra = theme.blank();
					out.print(color(color, extra + lines[i]));
				}
			}
		}
	}

	private String color(Color color, String text) {
		if (disableAnsiColors || color == NONE) {
			return text;
		}
		return color.toString() + text + NONE.toString();
	}

}
