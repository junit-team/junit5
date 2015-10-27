package org.junit.gen5.launcher;

import lombok.Data;

@Data
public final class TestIdentifier {

	private final String engineId;
	private final String testId;
	private final String displayName;

}
