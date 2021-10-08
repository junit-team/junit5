/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class IterationFilter implements Predicate<Integer> {

	private final Set<Integer> allowed = new HashSet<>();
	private Mode mode = Mode.EXPLICIT;

	public void allow(Set<Integer> indices) {
		if (this.mode == Mode.EXPLICIT) {
			this.allowed.addAll(indices);
		}
	}

	public void allowAll() {
		this.mode = Mode.ALLOW_ALL;
		this.allowed.clear();
	}

	@Override
	public boolean test(Integer index) {
		return allowed.isEmpty() || allowed.contains(index);
	}

	private enum Mode {
		EXPLICIT, ALLOW_ALL
	}
}
