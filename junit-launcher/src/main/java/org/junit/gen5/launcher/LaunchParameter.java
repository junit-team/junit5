/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.Serializable;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@API(Experimental)
public interface LaunchParameter extends Serializable {
	String getKey();

	String getValue();
}
