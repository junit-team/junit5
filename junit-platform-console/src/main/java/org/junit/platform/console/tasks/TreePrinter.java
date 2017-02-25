/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.console.tasks.Color.CONTAINER;
import static org.junit.platform.console.tasks.Color.NONE;
import static org.junit.platform.console.tasks.Color.SKIPPED;

import java.io.PrintWriter;
import java.util.Iterator;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestExecutionResult.Status;

/**
 * @since 1.0
 */
@API(Internal)
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
			String bullet = continuous ? theme.entry() : theme.end();
			String prefix = color(CONTAINER, indent + bullet);
			String caption = color(Color.valueOf(node.identifier), node.caption);
			String duration = color(CONTAINER, node.duration + " ms");
			String icon = color(SKIPPED, theme.skipped());
			if (node.result != null) {
				Color resultColor = Color.valueOf(node.result);
				icon = color(resultColor, theme.status(node.result));
				if (node.result.getStatus() != Status.SUCCESSFUL) {
					caption = color(resultColor, node.caption);
				}
			}
			if (node.reason != null) {
				caption = color(SKIPPED, node.caption);
			}
			out.print(prefix);
			out.print(" ");
			out.print(caption);
			if (node.duration > 10000 && node.children.isEmpty()) {
				// out.print(new String(new char[60 - (indent + bullet + node.caption).length()]).replace('\0', ' '));
				out.print(" ");
				out.print(duration);
			}
			out.print(" ");
			out.print(icon);
			out.println();
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

	private String color(Color color, String text) {
		if (disableAnsiColors || color == NONE) {
			return text;
		}
		return color.toString() + text + NONE.toString();
	}

}
