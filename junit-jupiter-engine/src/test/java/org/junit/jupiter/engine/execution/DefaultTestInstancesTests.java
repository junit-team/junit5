/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultTestInstancesTests {

	@Test
	void topLevelClass() {
		DefaultTestInstances instances = DefaultTestInstances.of(this);

		assertThat(instances.getInnermost()).isSameAs(this);
		assertThat(instances.getAll()).containsExactly(this);
		assertThat(instances.getEnclosing()).isEmpty();
		assertThat(instances.find(Object.class)).contains(this);
		assertThat(instances.find(String.class)).isEmpty();
	}

	@Test
	void nestedLevelClass() {
		DefaultTestInstancesTests outermost = this;
		Nested innermost = new Nested();
		DefaultTestInstances instances = DefaultTestInstances.of(DefaultTestInstances.of(outermost), innermost);

		assertThat(instances.getInnermost()).isSameAs(innermost);
		assertThat(instances.getAll()).containsExactly(outermost, innermost);
		assertThat(instances.getEnclosing()).containsExactly(outermost);
		assertThat(instances.find(Object.class)).contains(innermost);
		assertThat(instances.find(Nested.class)).contains(innermost);
		assertThat(instances.find(DefaultTestInstancesTests.class)).contains(outermost);
		assertThat(instances.find(String.class)).isEmpty();
	}

	class Nested {
	}

}
