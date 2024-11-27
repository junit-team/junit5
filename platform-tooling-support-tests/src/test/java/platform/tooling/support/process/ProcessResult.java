package platform.tooling.support.process;

import java.time.Duration;
import java.util.List;

public record ProcessResult(int exitCode, Duration duration, String stdOut, String stdErr) {
	public List<String> stdOutLines() {
		return stdOut.lines().toList();
	}
}
