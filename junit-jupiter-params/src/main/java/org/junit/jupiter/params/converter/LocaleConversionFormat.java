/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

/**
 * Enumeration of {@link java.util.Locale} conversion formats.
 *
 * @since 5.11
 */
@API(status = INTERNAL, since = "5.11")
public enum LocaleConversionFormat {

	/**
	 * The ISO 639 alpha-2 or alpha-3 language code format.
	 *
	 * @see java.util.Locale#Locale(String)
	 */
	ISO_639,

	/**
	 * The IETF BCP 47 language tag format.
	 *
	 * @see java.util.Locale#forLanguageTag(String)
	 */
	BCP_47

}
