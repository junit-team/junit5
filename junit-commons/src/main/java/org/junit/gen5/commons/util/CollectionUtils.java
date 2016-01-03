/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.util.Collection;

/**
 * Collection of utilities for working with {@link Collection Collections}.
 *
 * @since 5.0
 */
public class CollectionUtils {

	/**
	 * Read the only element of a collection of size 1.
	 *
	 * @param collection the collection to read the element from
	 * @return the only element of the collection
	 * @throws IllegalArgumentException if the collection is empty or contains
	 * more than one element
	 */
	public static <T> T getOnlyElement(Collection<T> collection) {
		Preconditions.condition(collection.size() == 1,
			() -> "collection must contain exactly one element: " + collection);
		return collection.iterator().next();
	}

}
