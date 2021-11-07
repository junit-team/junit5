/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// tag::user_guide_temp_dir_cleanup_mode[]
class TempDirCleanupModeDemo {

	@Test
	void fileTest(@TempDir(cleanup = ON_SUCCESS) Path tempDir) {
		// perform test
	}
}
// end::user_guide_temp_dir_cleanup_mode[]
