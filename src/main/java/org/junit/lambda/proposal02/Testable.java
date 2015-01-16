package org.junit.lambda.proposal02;

public interface Testable {
    default void before() {
    }
    void run() throws Exception;
    default void after() {
    }
}
