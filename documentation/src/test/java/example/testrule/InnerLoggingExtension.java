package example.testrule;

public class InnerLoggingExtension extends LoggingExtension {
	
	static final String position = "inner";

	@Override
	public String getPosition() {
		return position;
	}

}
