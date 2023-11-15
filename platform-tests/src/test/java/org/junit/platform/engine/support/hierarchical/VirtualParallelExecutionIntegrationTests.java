/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.jupiter.api.condition.JRE.JAVA_21;

import org.junit.jupiter.api.condition.EnabledOnJre;

@EnabledOnJre(value = JAVA_21, disabledReason = "Use Java 21 features")
public class VirtualParallelExecutionIntegrationTests extends ParallelExecutionIntegrationTests {

	@Override
	protected String getParallelExecutor() {
		return "virtual";
	}

}
