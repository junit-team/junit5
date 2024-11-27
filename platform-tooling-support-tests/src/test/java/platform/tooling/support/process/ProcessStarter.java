package platform.tooling.support.process;

import static org.codehaus.groovy.runtime.ProcessGroovyMethods.consumeProcessErrorStream;
import static org.codehaus.groovy.runtime.ProcessGroovyMethods.consumeProcessOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.output.TeeOutputStream;

public class ProcessStarter {

	public static ProcessStarter java() {
		return javaCommand(currentJdkHome(), "java");
	}

	private static Path currentJdkHome() {
		var executable = ProcessHandle.current().info().command().map(Path::of).orElseThrow();
		// path element count is 3 or higher: "<JAVA_HOME>/bin/java[.exe]"
		return executable.getParent().getParent().toAbsolutePath();
	}

	public static ProcessStarter java(Path javaHome) {
		return javaCommand(javaHome, "java");
	}

	private static ProcessStarter javaCommand(Path javaHome, String commandName) {
		return new ProcessStarter() //
				.executable(javaHome.resolve("bin").resolve(commandName)) //
				.putEnvironment("JAVA_HOME", javaHome.toString());
	}

	private Path executable;
	private Path workingDir;
	private final List<String> arguments = new ArrayList<>();
	private final Map<String, String> environment = new LinkedHashMap<>();

	public ProcessStarter executable(Path executable) {
		this.executable = executable;
		return this;
	}

	public ProcessStarter workingDir(Path workingDir) {
		this.workingDir = workingDir;
		return this;
	}

	public ProcessStarter addArguments(String... arguments) {
		this.arguments.addAll(List.of(arguments));
		return this;
	}

	public ProcessStarter putEnvironment(String key, String value) {
		environment.put(key, value);
		return this;
	}

	public ProcessResult startAndWait() throws InterruptedException {
		return start().waitFor();
	}

	public WatchedProcess start() {
		var command = Stream.concat(Stream.of(executable.toAbsolutePath().toString()), arguments.stream()).toList();
		try {
			var builder = new ProcessBuilder().command(command);
			if (workingDir != null) {
				builder.directory(workingDir.toFile());
			}
			builder.environment().putAll(environment);
			var start = Instant.now();
			var out = new ByteArrayOutputStream();
			var err = new ByteArrayOutputStream();
			var process = builder.start();
			var outThread = consumeProcessOutputStream(process, new TeeOutputStream(System.out, out));
			var errThread = consumeProcessErrorStream(process, new TeeOutputStream(System.err, err));
			return new WatchedProcess(start, process, new WatchedOutput(outThread, out),
					new WatchedOutput(errThread, err));
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to start process: " + command, e);
		}
	}
}
