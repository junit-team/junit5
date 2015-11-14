/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FindClassesInPackageTest {

	@Test
	// TODO Find out why this test won't run on Jenkins and fix it
	@Ignore("does not run on Jenkins")
	public void findAllClassesInThisPackage() throws IOException, ClassNotFoundException {
		List<Class<?>> classes = Arrays.asList(ReflectionUtils.findAllClassesInPackage("org.junit.gen5.commons"));
		System.out.println("Number of classes found: " + classes.size());
		for (Class<?> clazz : classes) {
			System.out.println(clazz.getName());
		}
		Assert.assertTrue("Should be at least 20 classes", classes.size() >= 20);
		Assert.assertTrue(classes.contains(NestedClassToBeFound.class));
		Assert.assertTrue(classes.contains(MemberClassToBeFound.class));
	}

	class MemberClassToBeFound {
	}

	static class NestedClassToBeFound {
	}

}
