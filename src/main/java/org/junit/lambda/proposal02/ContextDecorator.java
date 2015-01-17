package org.junit.lambda.proposal02;

import java.io.IOException;

public interface ContextDecorator {

    default public void before() throws IOException {

    }

    default public void after() throws IOException {

    }

    default public void beforeAll() throws IOException {

    }

    default public void afterAll() throws IOException {

    }
}
