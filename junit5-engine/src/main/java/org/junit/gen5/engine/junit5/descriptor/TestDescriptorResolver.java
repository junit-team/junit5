/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public interface TestDescriptorResolver<SPECIFICATION_ELEMENT extends TestPlanSpecificationElement, TEST_DESCRIPTOR extends TestDescriptor> {

	TEST_DESCRIPTOR resolve(TestDescriptor parent, SPECIFICATION_ELEMENT element);

	List<TestDescriptor> resolveChildren(TEST_DESCRIPTOR parent, SPECIFICATION_ELEMENT element);
}