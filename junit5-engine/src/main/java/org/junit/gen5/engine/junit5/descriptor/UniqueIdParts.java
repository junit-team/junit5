/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UniqueIdParts {

	private static final String SEPARATORS = ":$#";
	private final List<String> parts = new ArrayList<>();

	public UniqueIdParts(String uniqueId) {
		splitIntoParts(uniqueId);
	}

	private void splitIntoParts(String uniqueId) {
		String currentPart = "";
		for (char c : uniqueId.toCharArray()) {
			if (SEPARATORS.contains(Character.toString(c))) {
				parts.add(currentPart);
				currentPart = "";
			}
			currentPart += c;
		}
		parts.add(currentPart);
	}

	public String pop() {
		return parts.remove(0);
	}

	public String rest() {
		return parts.stream().collect(Collectors.joining());
	}
}
