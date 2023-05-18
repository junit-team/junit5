

package org.junit.platform.suite.engine.testsuites;

import org.junit.platform.suite.api.SelectMethod;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.testcases.MultipleTestsTestCase;

/**
 * @since 1.10
 */
@Suite
@SelectMethod(clazz = MultipleTestsTestCase.class, name = "test")
public class SelectMethodsSuite {
}
