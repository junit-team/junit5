/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

//tag::user_guide[]
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

@RunWith(org.junit.platform.runner.JUnitPlatform.class)
@SuiteDisplayName("JUnit Platform Suite Demo")
@SelectPackages("example")
//end::user_guide[]
@SuppressWarnings("deprecation")
@org.junit.platform.suite.api.ExcludeTags("exclude")
//tag::user_guide[]
public class JUnitPlatformSuiteDemo {
}
//end::user_guide[]
