package chronos.sys;

import lombok.Getter;
import java.time.LocalDateTime;

public class ScheduledJob {

    @Getter
    private final Job job;

    @Getter
    private LocalDateTime executionDateTime;

    public ScheduledJob(Job job) {
        this.job = job;

        ScheduleResponse response =
            job.getSchedule().nextExecution(LocalDateTime.now());

        this.executionDateTime =
            response != null ? response.getNextDateTime() : null;
    }
    
    
    public void refresh() {
    	
    	if (this.job.getSchedule().getClass() == IntervalSchedule.class)
    	{
    		ScheduleResponse resp = this.job.getSchedule().nextExecution(executionDateTime);
    		this.executionDateTime = resp!=null ? resp.getNextDateTime() : null ;
    	}
    	
    	return;
    	
    }
    
    
}