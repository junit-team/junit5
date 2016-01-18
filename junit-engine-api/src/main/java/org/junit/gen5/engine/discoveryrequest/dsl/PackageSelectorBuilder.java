/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discoveryrequest.dsl;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.discoveryrequest.PackageNameSelector;

/**
 * @since 5.0
 */
public class PackageSelectorBuilder {
	public static DiscoverySelector byPackageName(String packageName) {
		return new PackageNameSelector(packageName);
	}

	public static List<DiscoverySelector> byPackageNames(Collection<String> packageNames) {
		return packageNames.stream().map(PackageSelectorBuilder::byPackageName).collect(toList());
	}
}
