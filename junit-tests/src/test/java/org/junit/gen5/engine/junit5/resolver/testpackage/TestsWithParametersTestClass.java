/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver.testpackage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;

public class TestsWithParametersTestClass {
	@Test
	void test1(TestInfo testInfo) {
		assertThat(testInfo).isNotNull();
		assertThat(testInfo.getName()).isEqualTo("test1");
		assertThat(testInfo.getDisplayName()).isEqualTo("test1");
	}

	@Test
	@DisplayName("display name")
	void test2(TestInfo testInfo) {
		assertThat(testInfo).isNotNull();
		assertThat(testInfo.getName()).isEqualTo("test2");
		assertThat(testInfo.getDisplayName()).isEqualTo("display name");
	}
}
