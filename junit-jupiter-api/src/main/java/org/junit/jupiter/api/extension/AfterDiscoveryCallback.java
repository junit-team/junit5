/**
 * 
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
