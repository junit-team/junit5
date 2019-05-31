/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Access bridge to {@code Version.kt} declared in {@code buildSrc} project.
 *
 * @since 1.5
 */
public class Versions {

	private static final Map<String, String> MAP = new HashMap<>();

	static {
		var pattern = Pattern.compile(".*val\\s+(.+)\\s+=\\s+\"(.+)\"");
		try {
			var path = Path.of("../buildSrc/src/main/kotlin/Versions.kt").toAbsolutePath().normalize();
			for (var line : Files.readAllLines(path)) {
				var matcher = pattern.matcher(line);
				if (matcher.matches()) {
					var key = matcher.group(1);
					var value = matcher.group(2);
					MAP.put(key, value);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@code val junit4 = "4.12"}
	 * @param name denotes the name or key part of a library
	 * @param defaultVersion if not declared or found, use this version as default
	 */
	public static String version(String name, String defaultVersion) {
		return MAP.getOrDefault(name, defaultVersion);
	}
}
