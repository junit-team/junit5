/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

import java.util.List;
import java.util.Optional;

public interface CommandLineOptions {

	boolean isDisplayHelp();

	boolean isExitCodeEnabled();

	boolean isAnsiColorOutputDisabled();

	boolean isRunAllTests();

	boolean isHideDetails();

	Optional<String> getClassnameFilter();

	List<String> getTagsFilter();

	List<String> getArguments();

	List<String> getAdditionalClasspathEntries();

}
