/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.platform.gradle.plugin

import static org.apiguardian.api.API.Status.EXPERIMENTAL

import org.apiguardian.api.API
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.util.CollectionUtils

/**
 * Custom JavaExec task for the JUnit Platform Gradle plugin.
 *
 * @since 1.1
 */
@API(status = EXPERIMENTAL, since = "1.1")
class JUnitPlatformJavaExec extends JavaExec {

	@Optional
	@Input
	public FileCollection modulepath = null

	/**
	 * This implementation enhances the JVM arguments with all
	 * entries of the {@link #modulepath} field, if there is any.
	 */
	@Override
	List<String> getAllJvmArgs() {
		def args = super.getAllJvmArgs()
		if (modulepath != null && !modulepath.isEmpty()) {
			args.add('--module-path')
			args.add(CollectionUtils.join(File.pathSeparator, this.modulepath.getFiles()))
			args.addAll('--add-modules', 'ALL-MODULE-PATH')
		}
		return args
	}

}
