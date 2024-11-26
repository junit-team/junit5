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

import static java.lang.String.join;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * class provide access to printDiff function
 */
class DiffPrinter {
	private final TestPlan testPlan;

	public DiffPrinter(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	//print the difference of two print to out
	void printDiff(PrintWriter out, String expected, String actual, TestIdentifier testIdentifier) {
		char id = testIdentifier.getUniqueId().charAt(testIdentifier.getUniqueId().length() - 4);
		out.printf(" (%c) %s:", id == 's' ? '1' : id, describeTest(testIdentifier));
		boolean inlineDiffByWordFlag = false;
		if (expected.contains(" ") || actual.contains(" ")) {
			inlineDiffByWordFlag = true;
		}
		DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(
			inlineDiffByWordFlag).oldTag(f -> "~~").newTag(f -> "**").build();
		List<DiffRow> rows = generator.generateDiffRows(Arrays.asList(expected), Arrays.asList(actual));
		out.println();
		out.println("    | expected | actual |");
		out.println("    | -------- | ------ |");
		for (DiffRow row : rows) {
			out.printf("    | %s | %s |", row.getOldLine(), row.getNewLine());
			out.println();
		}
	}

	private String describeTest(TestIdentifier testIdentifier) {
		List<String> descriptionParts = new ArrayList<>();
		collectTestDescription(testIdentifier, descriptionParts);
		return join(":", descriptionParts);
	}

	private void collectTestDescription(TestIdentifier identifier, List<String> descriptionParts) {
		descriptionParts.add(0, identifier.getDisplayName());
		testPlan.getParent(identifier).ifPresent(parent -> collectTestDescription(parent, descriptionParts));
	}
}
