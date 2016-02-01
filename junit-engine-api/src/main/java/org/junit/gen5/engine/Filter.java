/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.function.Predicate;

import org.junit.gen5.commons.meta.API;

/**
 * Filters particular tests during/after test discovery.
 *
 * <p>Clients should not implement this interface directly but rather one of
 * its subinterfaces.
 *
 * @since 5.0
 * @see DiscoveryFilter
 */
@FunctionalInterface
@API(Internal)
public interface Filter<T> {

	FilterResult filter(T object);

	default Predicate<T> toPredicate() {
		return object -> filter(object).included();
	}

}
