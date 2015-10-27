
package org.junit.core;

import java.util.List;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestDescriptor {

	String getEngineId();

	String getTestId();

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must hold across an entire test plan, regardless of
	 * how many engines are used behind the scenes.
	 *
	 * <p>The UID is simply the concatenation of the {@linkplain #getEngineId engine ID}
	 * and the {@linkplain #getTestId test ID} separated by a colon ({@code ":"})
	 * &mdash; for example, {@code myEngine:test-description-unique-within-my-engine}.
	 */
	String getUniqueId();

	String getDisplayName();

	TestDescriptor getParent();

	List<TestDescriptor> getChildren();

	boolean isRoot();

	boolean isNode();

	boolean isLeaf();

	boolean isDynamic();

}
