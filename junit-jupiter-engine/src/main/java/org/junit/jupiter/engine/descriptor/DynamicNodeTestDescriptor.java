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

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * Base {@link TestDescriptor} for a {@link DynamicNode}.
 *
 * @since 5.0.3
 */
abstract class DynamicNodeTestDescriptor extends JupiterTestDescriptor {

	private final int index;

	DynamicNodeTestDescriptor(UniqueId uniqueId, int index, DynamicNode dynamicNode, TestSource testSource) {
		super(uniqueId, dynamicNode.getDisplayName(), testSource);
		this.index = index;
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
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
		return context.extend().withExtensionContext(null).build();
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
		return SkipResult.doNotSkip();
	}

}
