/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a <em>classpath root</em> so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can search for class
 * files or resources within the physical classpath &mdash; for example, to
 * scan for test classes.
 *
 * @since 1.0
 * @see ClasspathResourceSelector
 */
@API(Experimental)
public class ClasspathRootSelector implements DiscoverySelector {

	private final File classpathRoot;

	ClasspathRootSelector(File classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	/**
	 * Get the selected classpath root directory.
	 */
	public File getClasspathRoot() {
		return this.classpathRoot;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("classpathRoot", this.classpathRoot).toString();
	}

}
