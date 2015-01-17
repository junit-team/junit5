package org.junit.lambda.proposal02;

public @interface Decorate {
    Class<? extends ContextDecorator> value();
}
