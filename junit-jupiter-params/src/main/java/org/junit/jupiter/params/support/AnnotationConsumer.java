/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.support;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import org.junit.platform.commons.meta.API;

/**
 * {@code @AnnotationConsumer} is a {@code @FunctionalInterface} for consuming
 * annotations.
 *
 * <p>It is typically implemented by implementations of
 * {@link org.junit.jupiter.params.provider.ArgumentsProvider} and
 * {@link org.junit.jupiter.params.converter.ArgumentConverter} in order to
 * signal that they can {@link #accept} a certain annotation.
 *
 * @since 5.0
 */
@API(Experimental)
public interface AnnotationConsumer<A extends Annotation> extends Consumer<A> {
}
