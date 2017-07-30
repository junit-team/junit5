
package org.junit.jupiter.api.extension;

import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * Provides type-safe access to values in an {@link ExetensionContext.Store}.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * public class TimingExtension
 *     implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
 *   private static final StoredValues<Method, Instant> START_TIMES;
 * 
 *   @Override
 *   public void beforeTestExecution(ExtensionContext context) {
 *     START_TIMES.put(
 *         getStore(context), context.getRequiredTestMethod(), Instant.now());
 *   }
 * }
 * }</pre>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class StoredValues<K, V> {
	private final Class<V> valueType;

	/** Creates an instance that stores values of the given type. */
	public static <K, V> StoredValues<K, V> storing(Class<V> valueType) {
		return new StoredValues<K, V>(valueType);
	}

	/**
	 * Get the value that is stored under the supplied {@code key} from the
	 * given {@code store}.
	 *
	 * <p>If no value is stored in the store's current {@link ExtensionContext}
	 * for the supplied {@code key}, ancestors of the context will be queried
	 * for a value with the same {@code key} in the {@code Namespace} used to
	 * create the store.
	 *
	 * @param store the store to use to get the data
	 * @param key the key; never {@code null}
	 * @return the value; potentially {@code null}
	 * @see #get(Object, Class)
	 */
	public V get(Store store, K key) {
		return store.get(key, valueType);
	}

	/**
	 * Get the value that is stored under the supplied {@code key} from the
	 * given {@code store}.
	 *
	 * <p>If no value is stored in the store's current {@link ExtensionContext}
	 * for the supplied {@code key}, ancestors of the context will be queried
	 * for a value with the same {@code key} in the {@code Namespace} used to
	 * create this store. If no value is found for the supplied {@code key}, a
	 * new value will be computed by the {@code defaultCreator} (given the
	 * {@code key} as input), stored, and returned.
	 *
	 * @param store the store to use to get the data
	 * @param key the key; never {@code null}
	 * @param defaultCreator the function called with the supplied {@code key}
	 *        to create a new value; never {@code null}
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the value; potentially {@code null}
	 * @see #getOrComputeIfAbsent(Object, Function, Class)
	 */
	public V getOrComputeIfAbsent(Store store, K key, Function<K, V> defaultCreator) {
		return store.getOrComputeIfAbsent(key, defaultCreator, valueType);
	}

	/**
	 * Store a {@code value} for later retrieval under the supplied {@code key}
	 * in the given {@code store}.
	 *
	 * <p>A stored {@code value} is visible in child {@link ExtensionContext
	 * ExtensionContexts} for the store's {@code Namespace} unless they
	 * overwrite it.
	 *
	 * @param key the key under which the value should be stored; never
	 *        {@code null}
	 * @param value the value to store; may be {@code null}
	 */
	public void put(Store store, K key, V value) {
		store.put(key, value);
	}

	/**
	 * Remove the value that was previously stored under the supplied
	 * {@code key} in the given {@code store}.
	 *
	 * <p>The value will only be removed in the current
	 * {@link ExtensionContext}, not in ancestors.
	 *
	 * @param key the key; never {@code null}
	 * @return the previous value or {@code null} if no value was present for
	 *         the specified key
	 * @see #remove(Object, Class)
	 */
	public V remove(Store store, K key) {
		return store.remove(key, valueType);
	}

	private StoredValues(Class<V> valueType) {
		this.valueType = valueType;
	}
	
}
