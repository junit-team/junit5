/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.extension.AfterDiscoveryCallback;
import org.junit.platform.engine.TestDescriptor;

/**
 * @author swm16
 *
 */
public class DiscoveryReportExtension implements AfterDiscoveryCallback {

	/* (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.AfterDiscoveryCallback#afterDiscovery(java.lang.Object)
	 */
	@Override
	public void afterDiscovery(Object object) {
		if (object instanceof TestDescriptor) {
			TestDescriptor testDescriptor = (TestDescriptor) object;
			System.out.println("DiscoveryReportExtension.afterDiscovery(): UniqueId - " + testDescriptor.getUniqueId());
			System.out.println(
				"DiscoveryReportExtension.afterDiscovery(): DisplayName - " + testDescriptor.getDisplayName());
		}
		else {
			System.out.println("afterDiscovery(): ERROR - That wasn't a TestDescriptor");
		}
	}

}
