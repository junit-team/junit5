/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;

import org.junit.Test;

public class GroupDescriptorTests {
	@Test
	public void ensureThat_isTest_alwaysEvaluatesTo_False_ForATestGroup() throws Exception {
		GroupDescriptor testGroup = new GroupDescriptor(null, null);
		assertThat(testGroup.isTest()).isFalse();
	}

	@Test
	public void givenAnUniqueTestId_TestGroupReturnsTestId() throws Exception {
		GroupDescriptor testGroup = new GroupDescriptor("testID", null);
		assertThat(testGroup.getUniqueId()).isEqualTo("testID");
	}

	@Test
	public void givenADisplayName_TestGroupReturnsDisplayName() throws Exception {
		GroupDescriptor testGroup = new GroupDescriptor(null, "A descriptive display name");
		assertThat(testGroup.getDisplayName()).isEqualTo("A descriptive display name");
	}
}
