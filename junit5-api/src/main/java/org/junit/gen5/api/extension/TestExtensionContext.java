/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * {@code TestExtensionContext} encapsulates the <em>context</em> in which
 * the current test is being executed.
 *
 * @since 5.0
 */
@API(Experimental)
public interface TestExtensionContext extends ExtensionContext {

	Object getTestInstance();

}
