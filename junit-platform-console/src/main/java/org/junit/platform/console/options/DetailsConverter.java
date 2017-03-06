/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import joptsimple.util.EnumConverter;

import org.junit.platform.commons.meta.API;

/**
 * @since 1.0
 */
@API(Internal)
class DetailsConverter extends EnumConverter<Details> {

	DetailsConverter() {
		super(Details.class);
	}

}
