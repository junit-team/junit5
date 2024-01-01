/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ThirdPartyJars {

	private ThirdPartyJars() {
	}

	public static void copy(Path targetDir, String group, String artifact) throws Exception {
		Path source = Stream.of(System.getProperty("thirdPartyJars").split(File.pathSeparator)) //
				.filter(it -> it.replace(File.separator, "/").contains("/" + group + "/" + artifact + "/")) //
				.map(Path::of) //
				.findFirst() //
				.orElseThrow(() -> new AssertionError("Failed to find JAR file for " + group + ":" + artifact));
		Files.copy(source, targetDir.resolve(source.getFileName()), REPLACE_EXISTING);
	}
}
