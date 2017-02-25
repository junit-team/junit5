/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.nio.charset.Charset;

import org.junit.platform.commons.meta.API;

/**
 * @since 1.0
 */
@API(Internal)
class ConsoleLauncherWrapperResult {

	final String[] args;
	final Charset charset;
	final int code;
	final String out;
	final String err;

	ConsoleLauncherWrapperResult(String[] args, Charset charset, int code, String out, String err) {
		this.args = args;
		this.charset = charset;
		this.code = code;
		this.out = out;
		this.err = err;
	}
}
