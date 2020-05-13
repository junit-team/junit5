/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.suite.api.SuiteDisplayName;

class SuiteDescriptor extends AbstractTestDescriptor {

	SuiteDescriptor(Class<?> suiteClass) {
		super(UniqueId.root("suite", suiteClass.getName()), determineDisplayName(suiteClass),
			ClassSource.from(suiteClass));
	}

	private static String determineDisplayName(Class<?> suiteClass) {
		return AnnotationSupport.findAnnotation(suiteClass, SuiteDisplayName.class).map(SuiteDisplayName::value).orElse(
			suiteClass.getName());
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}
}
