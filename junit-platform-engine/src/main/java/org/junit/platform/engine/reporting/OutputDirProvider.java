/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.reporting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.platform.engine.TestDescriptor;

public interface OutputDirProvider {

	OutputDirProvider NOOP = __ -> Optional.empty();

	Optional<Path> createOutputDirectory(TestDescriptor testDescriptor) throws IOException;

}
