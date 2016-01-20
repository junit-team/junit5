/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import org.junit.gen5.junit4.runner.Classes;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Classes({ SampleTestCase.class, SucceedingTestCase.class, JUnit4TestCase.class })
public class JUnit4SamplesSuite {
}
