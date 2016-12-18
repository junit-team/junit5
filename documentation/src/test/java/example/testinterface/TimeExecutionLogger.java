package example.testinterface;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import example.timing.TimingExtension;

//tag::user_guide[]
@Tag("timed")
@ExtendWith(TimingExtension.class)
public interface TimeExecutionLogger {
}
//end::user_guide[]
