/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.subpackage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @since 5.9
 */
public class SuperClassWithPackagePrivateLifecycleMethodInDifferentPackageTestCase {

	protected boolean beforeEachInvoked = false;

	@BeforeEach
	void beforeEach() {
		this.beforeEachInvoked = true;
	}

	@Test
	void test() {
		assertThat(this.beforeEachInvoked).isTrue();
	}

}
