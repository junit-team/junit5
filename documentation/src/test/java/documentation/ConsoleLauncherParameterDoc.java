/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package documentation;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.console.ConsoleLauncher;

class ConsoleLauncherParameterDoc {

	@Test
	void generateHelpOutput() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		if (ConsoleLauncher.execute(new PrintStream(out), new PrintStream(err), "--help").getExitCode() != 0) {
			Assertions.fail(toString(err));
		}

		writeAsciiDoc("console_launcher_parameters.adoc", toString(out));
	}

	private void writeAsciiDoc(String fileName, String content) throws IOException {
		Path generatedAsciidocSource = Paths.get("build/generated/asciidoc");
		Files.createDirectories(generatedAsciidocSource);
		Path outputFile = generatedAsciidocSource.resolve(fileName);
		Files.write(outputFile, content.getBytes(forName("UTF-8")));
	}

	private String toString(ByteArrayOutputStream err) {
		return new String(err.toByteArray(), defaultCharset());
	}
}
