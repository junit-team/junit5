/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInPackageOnly;
import static org.junit.gen5.commons.util.ReflectionUtils.isAbstract;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;

public class ClassResolver extends JUnit5TestResolver {
	public static ClassTestDescriptor descriptorForParentAndClass(TestDescriptor parent, Class<?> testClass) {
		return new ClassTestDescriptor(parent.getUniqueId() + "/[class:" + testClass.getSimpleName() + "]", testClass);
	}

	private static final Logger LOG = Logger.getLogger(ClassResolver.class.getName());

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		List<ClassTestDescriptor> classDescriptors = new LinkedList<>();
		if (parent instanceof NewPackageTestDescriptor) {
			String packageName = ((NewPackageTestDescriptor) parent).getPackageName();
			classDescriptors.addAll(resolveTopLevelClassesInPackage(parent, packageName));
		}

		addChildrenAndNotify(parent, classDescriptors, discoveryRequest);
	}

	private List<ClassTestDescriptor> resolveTopLevelClassesInPackage(TestDescriptor parent, String packageName) {
		// @formatter:off
        return findAllClassesInPackageOnly(packageName, aClass -> true).stream()
                .filter(this::isTopLevelTestClass)
                .map(testClass -> descriptorForParentAndClass(parent, testClass))
                .collect(toList());
        // @formatter:on
	}

	private boolean isTopLevelTestClass(Class<?> candidate) {
		//please do not collapse into single return
		if (isAbstract(candidate))
			return false;
		if (candidate.isLocalClass())
			return false;
		if (candidate.isAnonymousClass())
			return false;
		return !candidate.isMemberClass();
	}
}
