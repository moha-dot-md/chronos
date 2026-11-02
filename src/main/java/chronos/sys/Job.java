package chronos.sys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.util.concurrent.Callable;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

// Only describes when is the action and the action itself.
public class Job {
    private String id;
    private String name;
    private Callable<JobResult> action;
    private JobResult result;
    private Schedule schedule;
    private Duration timeOut;
    private Priority priority;

}
