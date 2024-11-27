package platform.tooling.support.process;

import java.time.Duration;
import java.time.Instant;

public class WatchedProcess {

	private final Instant start;
	private final Process process;
	private final WatchedOutput out;
	private final WatchedOutput err;

	WatchedProcess(Instant start, Process process, WatchedOutput out, WatchedOutput err) {
		this.start = start;
		this.process = process;
		this.out = out;
		this.err = err;
	}

	ProcessResult waitFor() throws InterruptedException {
		try {
			int exitCode;
			Instant end;
			try {
				try {
					exitCode = process.waitFor();
					end = Instant.now();
				}
				catch (InterruptedException e) {
					process.destroyForcibly();
					throw e;
				}
			}
			finally {
				try {
					out.join();
				}
				finally {
					err.join();
				}
			}
			return new ProcessResult(exitCode, Duration.between(start, end), out.getStreamAsString(),
					err.getStreamAsString());
		}
		finally {
			process.destroyForcibly();
		}
	}
}
