package chronos.sys;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;


@AllArgsConstructor
@Data
public class IntervalSchedule implements Schedule {

    private final Duration interval;
    private final LocalDateTime firstExecution;

    @Override
    public ScheduleResponse nextExecution(LocalDateTime from) {

        if (firstExecution.isAfter(from))
            return new ScheduleResponse(firstExecution,
                    ScheduleStatus.SCHEDULED);


        Duration elapsed = Duration.between(firstExecution, from);
        long intervals = elapsed.dividedBy(interval);
        LocalDateTime nextExecution = firstExecution.
                plus(interval.
                        multipliedBy(intervals + 1));
        return new ScheduleResponse(
                nextExecution,
                ScheduleStatus.SCHEDULED);
    }
}
