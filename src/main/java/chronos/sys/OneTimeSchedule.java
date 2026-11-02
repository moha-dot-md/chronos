package chronos.sys;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
public class OneTimeSchedule implements Schedule{

    private final LocalDateTime executionDate;
    
    @Override
    public ScheduleResponse nextExecution(LocalDateTime from) {
       if (executionDate.isAfter(from)) {
           return new ScheduleResponse(executionDate,
                   ScheduleStatus.SCHEDULED);
       }
     
       
       return new ScheduleResponse(null,
               ScheduleStatus.NOT_SCHEDULED);
    }
}
