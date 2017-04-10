/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * @since 1.0
 */
class ServiceLoaderTestExecutionListenerRegistry {

	private static final Logger LOG = Logger.getLogger(ServiceLoaderTestExecutionListenerRegistry.class.getName());

	Iterable<TestExecutionListener> loadListeners() {
		Iterable<TestExecutionListener> listeners = ServiceLoader.load(TestExecutionListener.class,
			ClassLoaderUtils.getDefaultClassLoader());
		LOG.config(() -> "Loaded TestExecutionListener instances: "
				+ stream(listeners.spliterator(), false).map(Object::toString).collect(toList()));
		return listeners;
	}

}
