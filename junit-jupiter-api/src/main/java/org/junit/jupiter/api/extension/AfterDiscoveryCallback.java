/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import org.junit.platform.commons.meta.API;

/**
 * @author swm16
 *
 */
@FunctionalInterface
@API(API.Usage.Experimental)
public interface AfterDiscoveryCallback extends Extension {

	void afterDiscovery(Object testDescriptor) throws Exception;

}
