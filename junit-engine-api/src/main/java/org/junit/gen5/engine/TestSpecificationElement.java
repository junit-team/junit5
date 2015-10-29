/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Collections;
import java.util.Iterator;

public interface TestSpecificationElement extends TestPlanSpecification {

	@Override
	default public Iterator<TestPlanSpecification> iterator() {
		return Collections.<TestPlanSpecification> singleton(this).iterator();
	}

}