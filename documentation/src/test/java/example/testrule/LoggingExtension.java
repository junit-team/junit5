package example.testrule;

import java.util.logging.Logger;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;

public abstract class LoggingExtension implements AfterTestExecutionCallback, BeforeTestExecutionCallback {
	
//	private static final Logger logger = Logger.getLogger("");

	public abstract String getPosition();

	@Override
	public void beforeTestExecution(TestExtensionContext context) throws Exception {
//		logger.info("starting " + getPosition() + " extension");
		context.publishReportEntry("before position", "starting " + getPosition() + " extension");
	}

	@Override
	public void afterTestExecution(TestExtensionContext context) throws Exception {
//		logger.info("finished " + getPosition() + " extension");
		context.publishReportEntry("after position", "finished " + getPosition() + " extension");
	}

}
