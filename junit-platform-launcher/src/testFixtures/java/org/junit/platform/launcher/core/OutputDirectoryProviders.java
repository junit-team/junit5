package org.junit.platform.launcher.core;

import static com.google.common.jimfs.Configuration.unix;

import java.io.IOException;

import com.google.common.jimfs.Jimfs;

import org.junit.platform.engine.reporting.OutputDirectoryProvider;

public class OutputDirectoryProviders {

	public static OutputDirectoryProvider inMemoryOutputDirectoryProvider() {
		@SuppressWarnings("resource") var fileSystem = Jimfs.newFileSystem(unix());
		return new HierarchicalOutputDirectoryProvider(() -> fileSystem.getPath("/")) {
			@Override
			public void close() throws IOException {
				fileSystem.close();
			}
		};
	}
}
