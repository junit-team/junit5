package org.junit.lambda.proposal02;

public @interface Decorate {
    Class<? extends TestComponentDecorator> value();
}
