/*
 * Copyright 2015-2024 the original author or authors.
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

	// https://issues.apache.org/jira/browse/MNG-8248
	static final Map<String, String> FOR_JDK24_AND_LATER = JRE.currentVersion().compareTo(JRE.JAVA_24) >= 0 //
			? Map.of("MAVEN_OPTS", "--enable-native-access=ALL-UNNAMED") //
			: Map.of();

	private MavenEnvVars() {
	}

}
