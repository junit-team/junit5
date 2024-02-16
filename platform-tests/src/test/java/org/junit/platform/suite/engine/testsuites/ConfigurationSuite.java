package org.junit.platform.suite.engine.testsuites;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.testcases.ConfigurationSensitiveTestCase;

import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

@Suite
@ConfigurationParameter(key = DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME, value = "per_class")
@SelectClasses(ConfigurationSensitiveTestCase.class)
public class ConfigurationSuite {
}
