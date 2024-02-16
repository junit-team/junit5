package org.junit.platform.suite.engine.testcases;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 1.10.2
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ConfigurationSensitiveTestCase {

    boolean shared;

    @Test
    void test1(){
        shared = true;
    }

    @Test
    void test2(){
        assertTrue(shared);
    }

}
