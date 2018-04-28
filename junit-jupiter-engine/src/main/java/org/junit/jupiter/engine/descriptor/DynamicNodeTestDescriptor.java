/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.Set;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Base {@link TestDescriptor} for a {@link DynamicNode}.
 *
 * @since 5.0.3
 */
abstract class DynamicNodeTestDescriptor extends JupiterTestDescriptor {

	private final int index;
	private final Set<TestTag> testTags;

	DynamicNodeTestDescriptor(UniqueId uniqueId, int index, DynamicNode dynamicNode, TestSource testSource,
			Set<TestTag> testTags) {
		super(uniqueId, dynamicNode.getDisplayName(), testSource);
		this.index = index;
		this.testTags = testTags;
	}

	@Override
	public Set<TestTag> getTags() {
		return testTags;
	}

	@Override
	public String getLegacyReportingName() {
		// @formatter:off
		return getParent()
				.map(TestDescriptor::getLegacyReportingName)
				.orElseGet(this::getDisplayName)
						+ "[" + index + "]";
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		return context.extend().withExtensionContext(null).build();
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) {
		return SkipResult.doNotSkip();
	}

}
