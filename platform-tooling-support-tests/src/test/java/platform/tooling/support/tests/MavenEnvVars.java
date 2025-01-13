/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import java.util.Map;

import org.junit.jupiter.api.condition.JRE;

final class MavenEnvVars {

	private static final Map<String, String> FOR_JDK24_AND_LATER = Map.of("MAVEN_OPTS", String.join(" ", //
		"--enable-native-access=ALL-UNNAMED", // https://issues.apache.org/jira/browse/MNG-8248
		"--sun-misc-unsafe-memory-access=allow" // https://issues.apache.org/jira/browse/MNG-8399
	));

	static Map<String, String> forJre(JRE jre) {
		return jre.compareTo(JRE.JAVA_24) >= 0 ? FOR_JDK24_AND_LATER : Map.of();
	}

	private MavenEnvVars() {
	}
}
