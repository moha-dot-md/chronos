package chronos.sys;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Callable;

public class Main {
    public static void main(String[] args) {
    	
     final Chronos chronos = new Chronos(4);
       
    		Callable<JobResult> print = () -> {
				JobResult jr = new JobResult();
				jr.setStartPeriod(LocalDateTime.now());
    			System.out.println("Print hello world");
				jr.setStatus(JobStatus.SUCCESS);
				jr.setOutput("Print hello world");
				jr.setEndPeriod(LocalDateTime.now());
				return jr;
    		};
    		
    		Callable<JobResult> write = () -> {
    			JobResult jr = new JobResult();
				PrintWriter writer = new PrintWriter("b_file.txt");
				jr.setStartPeriod(LocalDateTime.now());
				jr.setStatus(JobStatus.RUNNING);
				writer.append("This is a line for the test");
				Thread.sleep(8000);
				writer.close();
				jr.setEndPeriod(LocalDateTime.now());
				return jr;
				
    		};
    	
    	
    		Job job = Job.builder()
    				.id("1")
    				.name("printer")
    				.action(print)
    				.schedule(new IntervalSchedule(Duration.ofMinutes(1),
    												LocalDateTime.of(LocalDate.now(), 
    														         LocalTime.of(00, 10))))
    				.timeOut(Duration.ofSeconds(10))
    				.priority(Priority.HIGH)
    				.build();
    			
    		
    		Job job1 = Job.builder()
    				.id("1")
    				.name("writer")
    				.action(write)
    				.schedule(new OneTimeSchedule(LocalDateTime.of(LocalDate.now(), 
					         LocalTime.of(00, 20))))
    				.timeOut(Duration.ofSeconds(5))
    				.priority(Priority.LOW)
    				.build();    		
    		
    		ScheduledJob scheduledJob = new ScheduledJob(job);
    		ScheduledJob scheduledJob1 = new ScheduledJob(job1);

    		
//    		chronos.addJob(scheduledJob);
    		chronos.addJob(scheduledJob1);
    		

    		
    }

}
