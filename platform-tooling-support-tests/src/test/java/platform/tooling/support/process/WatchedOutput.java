package platform.tooling.support.process;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class WatchedOutput {

	private static final Charset CHARSET = Charset.forName(System.getProperty("native.encoding"));

	private final Thread thread;
	private final ByteArrayOutputStream stream;

	WatchedOutput(Thread thread, ByteArrayOutputStream stream) {
		this.thread = thread;
		this.stream = stream;
	}

	void join() throws InterruptedException {
		thread.join();
	}

	public String getStreamAsString() {
		return stream.toString(CHARSET);
	}
}
