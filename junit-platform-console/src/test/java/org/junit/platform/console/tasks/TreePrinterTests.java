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

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.platform.engine.TestExecutionResult.aborted;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;

class TreePrinterTests {

	private final Charset charset = StandardCharsets.UTF_8;
	private final ByteArrayOutputStream stream = new ByteArrayOutputStream(1000);
	private final PrintWriter out = new PrintWriter(new OutputStreamWriter(stream, charset));

	private List<String> actual() {
		try {
			out.flush();
			return Arrays.asList(stream.toString(charset.name()).split("\\R"));
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(charset.name() + " is an unsupported encoding?!", e);
		}
	}

	@Test
	void emptyTree() {
		new TreePrinter(out, Theme.UTF_8, true).print(new TreeNode("<root>"));
		assertIterableEquals(Collections.singletonList("╷"), actual());
	}

	@Test
	void emptyEngines() {
		TreeNode root = new TreeNode("<root>");
		root.addChild(new TreeNode(createEngineId("e-0", "engine zero"), "none"));
		root.addChild(new TreeNode(createEngineId("e-1", "engine one")).setResult(successful()));
		root.addChild(new TreeNode(createEngineId("e-2", "engine two")).setResult(failed(null)));
		root.addChild(new TreeNode(createEngineId("e-3", "engine three")).setResult(aborted(null)));
		new TreePrinter(out, Theme.UTF_8, true).print(root);
		assertIterableEquals(
			Arrays.asList( //
				"╷", //
				"├─ engine zero ↷", //
				"├─ engine one ✔", //
				"├─ engine two ✘", //
				"└─ engine three ■"), //
			actual());
	}

	private TestIdentifier createEngineId(String uniqueId, String displayName) {
		return TestIdentifier.from(new AbstractTestDescriptor(UniqueId.forEngine(uniqueId), displayName) {
			@Override
			public boolean isContainer() {
				return false;
			}

			@Override
			public boolean isTest() {
				return false;
			}
		});
	}
}
