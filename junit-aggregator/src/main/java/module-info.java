module org.junit.aggregator {

	requires static transitive org.apiguardian.api;
	requires static transitive org.jspecify;

	requires transitive org.junit.jupiter;
	requires org.junit.platform.launcher;
	requires org.junit.platform.console;

	exports org.junit.aggregator;

	uses java.util.spi.ToolProvider;

}
