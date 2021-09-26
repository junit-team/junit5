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

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirStrategy;

// tag::user_guide_temp_dir_strategy[]
@TempDirStrategy(cleanupMode = TempDirStrategy.CleanupMode.NEVER)
class TempDirStrategyDemo {

	@Test
	void fileTest(@TempDir Path tempDir) {
		// perform test
	}
}
// end::user_guide_temp_dir_strategy[]
