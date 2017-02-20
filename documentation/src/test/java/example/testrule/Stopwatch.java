package example.testrule;

import java.util.concurrent.TimeUnit;

/**
 * This class encapsulates the duration of a test run with nanosecond accuracy
 * (or to the best accuracy available) and allows the retrieval of the runtime
 * at any point during a test run.  Together with the StopwatchExtension, this
 * class provides the same functionality as the JUnit 4 Stopwatch.
 */
public class Stopwatch {
	
	private long start;
	
	/**
	 * Constructs a Stopwatch with a start time equal to the system's current
	 * nano-time.
	 */
	public Stopwatch() {
		this(System.nanoTime());
	}
	
	/**
	 * Constructs a Stopwatch using a provided arbitrary start time.
	 * 
	 * @param start Presumably a start time that is something other than the
	 * system's current nano-time.
	 */
	public Stopwatch(long start) {
		this.start = start;
	}
	
	/**
	 * Returns the current runtime of the test converted to the units specified
	 * by the passed TimeUnit.
	 * 
	 * @param timeUnit The output units of the runtime measurement.
	 * @return The runtime converted to the specified units.
	 */
	public long runtime(TimeUnit timeUnit) {
		return timeUnit.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
	}

}
