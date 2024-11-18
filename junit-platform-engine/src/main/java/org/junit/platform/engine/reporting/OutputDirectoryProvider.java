/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.reporting;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;
import java.nio.file.Path;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;

@API(status = EXPERIMENTAL, since = "1.12")
public interface OutputDirectoryProvider {

	Path getRootDirectory();

	Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException;

}
