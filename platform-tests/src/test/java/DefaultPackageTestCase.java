
/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PackageUtils;

/**
 * Simple test case that is used to verify proper support for classpath scanning
 * within the <em>default</em> package.
 *
 * @since 1.0
 */
public class DefaultPackageTestCase {

	@Test
	void test() {
		// do nothing
	}

	@Test
	void getAttributeFromDefaultPackageMemberIsEmpty() {
		assertFalse(PackageUtils.getAttribute(DefaultPackageTestCase.class, Object::toString).isPresent());
	}

}
