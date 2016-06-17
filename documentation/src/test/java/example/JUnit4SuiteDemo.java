/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

//tag::user_guide[]
import org.junit.gen5.junit4.runner.JUnitPlatform;
import org.junit.gen5.junit4.runner.Packages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@Packages("example")
//end::user_guide[]
@org.junit.gen5.junit4.runner.ExcludeTags("exclude")
//tag::user_guide[]
public class JUnit4SuiteDemo {
}
//end::user_guide[]
