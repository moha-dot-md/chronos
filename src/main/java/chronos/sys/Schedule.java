package chronos.sys;
import java.time.LocalDateTime;

@FunctionalInterface
public interface Schedule {

    ScheduleResponse nextExecution(LocalDateTime from);

}
