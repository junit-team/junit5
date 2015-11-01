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
		parts.add(uniqueId);
	}

	public String pop() {
		return null;
	}

	public String rest() {
		return parts.stream().collect(Collectors.joining());
	}
}
