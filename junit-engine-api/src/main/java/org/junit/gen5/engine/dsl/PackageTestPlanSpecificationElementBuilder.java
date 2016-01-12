/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.dsl;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import org.junit.gen5.engine.PackageSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;

public class PackageTestPlanSpecificationElementBuilder {
	public static TestPlanSpecificationElement forPackage(String packageName) {
		return new PackageSpecification(packageName);
	}

	public static List<TestPlanSpecificationElement> forPackages(Collection<String> packageNames) {
		return packageNames.stream().map(PackageTestPlanSpecificationElementBuilder::forPackage).collect(toList());
	}
}
