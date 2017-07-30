
package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.meta.API;

/**
 * Provides isolated, type-safe access to values in a {@link Store}.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * public class TimingExtension
 *     implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
 *   private static final StoredValues<Method, Instant> START_TIMES
 *       = new StoredValues<>();
 * 
 *   @Override
 *   public void beforeTestExecution(ExtensionContext context) {
 *     START_TIMES.put(
 *         getStore(context), context.getRequiredTestMethod(), Instant.now());
 *   }
 * }
 * }</pre>
 *
 * <p>Values stored using an instance of this class cannot be accessed by other
 * classes using the {@code Store} directly.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@API(Experimental)
public final class StoredValues<K, V> {

	/**
	 * Returns the value that is stored under the supplied {@code key} from the
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
	@SuppressWarnings("unchecked")
	public V get(Store store, K key) {
		return (V) store.get(wrap(key));
	}

	/**
	 * Returns the value that is stored under the supplied {@code key} from the
	 * given {@code store}, computing a value if there is no stored value.
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
	@SuppressWarnings("unchecked")
	public V getOrComputeIfAbsent(Store store, K key, Function<K, V> defaultCreator) {
		return (V) store.getOrComputeIfAbsent(
			wrap(key),
			k -> defaultCreator.apply(k.key));
	}

	/**
	 * Stores a {@code value} for later retrieval under the supplied {@code key}
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
		store.put(wrap(key), value);
	}

	/**
	 * Removes the value that was previously stored under the supplied
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
	@SuppressWarnings("unchecked")
	public V remove(Store store, K key) {
		return (V) store.remove(wrap(key));
	}

	private KeyWrapper<K> wrap(K key) {
		return new KeyWrapper<>(key);
	}

	/** Wraps a key, to provide isolation from other keys of the same type. */
	private static class KeyWrapper<K> {
		private final K key;

		KeyWrapper(K key) {
			this.key = key;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof KeyWrapper)) {
				return false;
			}
			KeyWrapper that = (KeyWrapper) obj;
			if (key == null) {
				return that.key == null;
			}
			return key.equals(that.key);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(key);
		}

	}
}
