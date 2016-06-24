/**
 * 
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
