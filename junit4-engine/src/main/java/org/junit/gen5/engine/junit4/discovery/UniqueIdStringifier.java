/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import java.io.Serializable;
import java.util.function.Function;

class UniqueIdStringifier implements Function<Serializable, String> {

	@Override
	public String apply(Serializable uniqueId) {
		// TODO #40 only use toString() for known classes; otherwise, serialize
		return String.valueOf(uniqueId);
	}

}
