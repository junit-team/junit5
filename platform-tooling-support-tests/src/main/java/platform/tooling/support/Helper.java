/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @since 1.3
 */
public class Helper {

	private static Properties gradleProperties = new Properties();

	static {
		try {
			gradleProperties.load(Files.newInputStream(Paths.get("..", "gradle.properties")));
		}
		catch (Exception e) {
			throw new AssertionError("loading gradle.properties failed", e);
		}
	}

	public static String version(String module) {
		if (module.startsWith("junit-jupiter")) {
			return gradleProperties.getProperty("version");
		}
		if (module.startsWith("junit-platform")) {
			return gradleProperties.getProperty("platformVersion");
		}
		if (module.startsWith("junit-vintage")) {
			return gradleProperties.getProperty("vintageVersion");
		}
		throw new AssertionError("module name is unknown: " + module);
	}

}
