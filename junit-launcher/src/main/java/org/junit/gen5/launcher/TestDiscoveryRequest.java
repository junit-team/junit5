package org.junit.gen5.launcher;

import org.junit.gen5.engine.*;

import java.util.*;

/**
 * Created by mmerdes on 19.01.16.
 */
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	void addSelector(DiscoverySelector selector);

	void addSelectors(Collection<DiscoverySelector> selectors);

	void addEngineIdFilter(EngineIdFilter engineIdFilter);

	void addEngineIdFilters(Collection<EngineIdFilter> engineIdFilters);

	void addFilter(DiscoveryFilter<?> discoveryFilter);

	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	List<EngineIdFilter> getEngineIdFilters();

	List<PostDiscoveryFilter> getPostDiscoveryFilters();

	boolean acceptDescriptor(TestDescriptor testDescriptor);
}
