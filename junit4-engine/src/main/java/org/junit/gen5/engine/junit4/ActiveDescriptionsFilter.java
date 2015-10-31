/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static java.util.stream.Collectors.joining;

import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

class ActiveDescriptionsFilter extends Filter {

	private final Set<Description> activeDescriptions;

	public ActiveDescriptionsFilter(Set<Description> activeDescriptions) {
		this.activeDescriptions = activeDescriptions;
	}

	@Override
	public boolean shouldRun(Description description) {
		return isDirectMatch(description) || descendantMatches(description);
	}

	private boolean isDirectMatch(Description description) {
		return activeDescriptions.contains(description);
	}

	private boolean descendantMatches(Description description) {
		return description.getChildren().stream().anyMatch(this::shouldRun);
	}

	@Override
	public String describe() {
		return "Any description of "
				+ activeDescriptions.stream().map(Description::getDisplayName).collect(joining(", "));
	}
}