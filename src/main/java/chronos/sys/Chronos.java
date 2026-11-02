// The orchestrator
package chronos.sys;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



// This is the main orchestrator
/*
 * It requires four components
 * 1. A PriorityQueue that holds the entered job to be done and orders them by schedule earliest and priority
 * 2. Schedule coordinator thread that checks when is the execution time (It is separate from the pool so we do not await availability)
 * 3. Executor Workers that execute the Callable<V> actions in the Job objects
 * 4. Timeout supervision it checks if the timeout of a Job is hit to stop it*/

public class Chronos {

    private final ExecutorService executor;
   
    // PriorityQueue is not thread-safe one can add and at the same time the other removes
    // _which can lead to an inconsistent execution so we are going to use ReenterantLock for mutual exclusion (mutex)
    private final PriorityQueue<ScheduledJob> JobQueue
            = new PriorityQueue<>(
                    Comparator.comparing(ScheduledJob::getExecutionDateTime)
                            .thenComparing(sj -> sj.getJob().getPriority().getValue())
    );

   
    private final Lock queueLock = new ReentrantLock();
    
    // This condition is for if the queue changes the Schedule coordinator thread wakes up
    private final Condition queueChanged = queueLock.newCondition();
    
    private final Thread scheduleCoordinator;
    
    private final ScheduledExecutorService timeoutSupervisor =
            Executors.newSingleThreadScheduledExecutor();
    
    
    private boolean running = true;
    
    
    public Chronos(int threadsnum) {
    	int workers = threadsnum > 0 ? threadsnum : 4;
    	executor = Executors.newFixedThreadPool(workers);
    	scheduleCoordinator = new Thread(
    				this::coordinateSchedule,
    				"schedule-coordinator-thread"
    			);
    	scheduleCoordinator.start();

    	
    	System.out.println("Chronos present and correct please.");
    }

    
    

private void coordinateSchedule() {

    while (true) {
        ScheduledJob dueJob = null;

        queueLock.lock();

        try {
            while (running) {
                ScheduledJob nextJob = JobQueue.peek();

                if (nextJob == null) {
                    queueChanged.await();
                    continue;
                }

                // Defensive check: invalid entries must never be executed.
                if (nextJob.getExecutionDateTime() == null) {
                    JobQueue.poll();
                    System.err.println(
                        "Discarding scheduled job with null execution time"
                    );
                    continue;
                }

                long nanosToWait = Duration.between(
                        LocalDateTime.now(),
                        nextJob.getExecutionDateTime()
                ).toNanos();

                
                if (nanosToWait > 0) {
                    queueChanged.awaitNanos(nanosToWait);
                    continue;
                }

                /*
                 * Le soucis est que si on enleve un objet qui doit etre reccurent,
                 * c'est un probleme car son prochaine execution ne va jamais avoir lieu
                 * On peut faire un sorte de verifier si l'objet Schedule est de type Interval
                 * on ne va pas le supprimer mais on le garder mais on utilise le refresh que je viens d'ajouter dans le ScheduledJob */
                dueJob = JobQueue.poll(); 
                break;
            }

            if (!running) {
                return;
            }

        } catch (InterruptedException e) {
            if (!running) {
                return;
            }

            Thread.currentThread().interrupt();
            return;

        } finally {
            queueLock.unlock();
        }

        if (dueJob != null) {
            ScheduledJob readyJob = dueJob;
            
            if (dueJob.getJob().getSchedule().getClass() == IntervalSchedule.class)
            {
            	dueJob.refresh();
            	JobQueue.add(dueJob);
            	
            }
            
            executor.submit(() -> execute(readyJob));

        }
    }
}

    
    public void addJob(ScheduledJob sj) {
    	 if (sj == null
                 || sj.getJob() == null
                 || sj.getJob().getId() == null) {
             throw new IllegalArgumentException("Invalid scheduled job");
         }
    	queueLock.lock();
    	try {
    		if (!running) {
                throw new IllegalStateException("Chronos is shut down");
            }
            JobQueue.add(sj);
         // Wake the coordinator so it can recheck the queue head.
            queueChanged.signal();
    	}
    	finally {
    		queueLock.unlock();
    	}
     }


    private JobResult execute(ScheduledJob scheduledJob) {
        Job job = scheduledJob.getJob();
        LocalDateTime startTime = LocalDateTime.now();

        AtomicReference<ExecutionState> state =
                new AtomicReference<>(ExecutionState.RUNNING);

        Thread worker = Thread.currentThread();

        ScheduledFuture<?> timeoutTask = timeoutSupervisor.schedule(
                () -> {
                    if (state.compareAndSet(
                            ExecutionState.RUNNING,
                            ExecutionState.TIME_OUT)) 
                    {
                        worker.interrupt();
                    }
                },
                job.getTimeOut().toNanos(),
                TimeUnit.NANOSECONDS
        );

        JobResult result;

        try {
            result = job.getAction().call();

            if (result == null) {
                result = new JobResult();
                result.setStatus(JobStatus.FAILED);
                result.setError("Action returned a null JobResult");
            }

            // Completion wins only if the timeout has not already fired.
            if (state.compareAndSet(
                    ExecutionState.RUNNING,
                    ExecutionState.COMPLETED)) {
                // Preserve the status and output returned by the action.
            } else {
                result.setStatus(JobStatus.SKIPPED);
                result.setError("Job execution timed out");
            }

        } catch (Exception e) {
            result = new JobResult();

            if (state.get() == ExecutionState.TIME_OUT) {
                result.setStatus(JobStatus.SKIPPED);
                result.setError("Job execution timed out");
            } else {
                state.compareAndSet(
                        ExecutionState.RUNNING,
                        ExecutionState.COMPLETED
                );

                result.setStatus(JobStatus.FAILED);
                result.setError(e.getMessage());
            }

        } finally {
            timeoutTask.cancel(false);

            // Clear a timeout interrupt before this pool thread is reused.
            if (state.get() == ExecutionState.TIME_OUT) {
                Thread.interrupted();
            }
        }

        result.setStartPeriod(startTime);
        result.setEndPeriod(LocalDateTime.now());

        System.out.println("Result: "+ result);
        return result;
    }
    
    
    public void shutdown() {
        queueLock.lock();
        try {
            running = false;
            queueChanged.signalAll();
        } finally {
            queueLock.unlock();
        }

        scheduleCoordinator.interrupt();
        executor.shutdown();
        timeoutSupervisor.shutdown();
    }
}