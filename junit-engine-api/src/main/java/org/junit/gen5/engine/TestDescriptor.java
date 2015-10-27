
package org.junit.gen5.engine;

import java.util.List;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestDescriptor {

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 *
	 * <p>The ID is simply the concatenation of the {@linkplain #getEngineId engine ID}
	 * and the {@linkplain #getTestId test ID} separated by a colon ({@code ":"})
	 * &mdash; for example, {@code myEngine:test-description-unique-within-my-engine}.
	 */
	String getId();

	String getEngineId();

	String getTestId();

	String getDisplayName();

	TestDescriptor getParent();

	List<TestDescriptor> getChildren();

	boolean isRoot();

	boolean isNode();

	boolean isLeaf();

	boolean isDynamic();

}
