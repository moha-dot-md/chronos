
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

import chronos.sys.jobtypes.Job;
import chronos.sys.jobtypes.ScheduledJob;
import chronos.sys.scheduleTypes.IntervalSchedule;

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

/**
 * Main orchestrator of the Chronos scheduling engine.
 *
 * <p>Chronos coordinates the scheduling and execution of jobs.
 * It manages a priority queue of scheduled jobs, a dedicated
 * scheduling coordinator, a pool of worker threads, and a
 * timeout supervisor.</p>
 *
 * <p>The execution lifecycle is divided into four components:</p>
 * <ol>
 *   <li>A priority queue orders jobs by execution date and priority.</li>
 *   <li>A dedicated coordinator monitors the queue and dispatches
 *       jobs when they become due.</li>
 *   <li>A worker pool executes job actions concurrently.</li>
 *   <li>A timeout supervisor monitors execution deadlines and
 *       interrupts workers when necessary.</li>
 * </ol>
 *
 * <p>Queue access is protected by a {@link ReentrantLock}
 * because {@link PriorityQueue} is not thread-safe.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
public class Chronos {

    /**
     * Thread pool responsible for executing job actions.
     */
    private final ExecutorService executor;

    /**
     * Priority queue containing scheduled job occurrences.
     *
     * <p>Jobs are ordered first by their execution date and then
     * by their priority value. A lower priority value is ordered
     * before a higher one.</p>
     *
     * <p>Access to this queue must be protected by {@link #queueLock}.</p>
     */
    private final PriorityQueue<ScheduledJob> JobQueue =
            new PriorityQueue<>(
                    Comparator.comparing(ScheduledJob::getExecutionDateTime)
                            .thenComparing(sj -> sj.getJob().getPriority().getValue())
            );

    /**
     * Mutual-exclusion lock protecting access to {@link #JobQueue}.
     */
    private final Lock queueLock = new ReentrantLock();

    /**
     * Condition used to notify the scheduling coordinator when
     * the queue changes.
     */
    private final Condition queueChanged = queueLock.newCondition();

    /**
     * Dedicated thread responsible for monitoring the scheduling queue
     * and dispatching jobs when their execution time is reached.
     */
    private final Thread scheduleCoordinator;

    /**
     * Single-threaded scheduler responsible for supervising job timeouts.
     */
    private final ScheduledExecutorService timeoutSupervisor =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Indicates whether the Chronos scheduling engine is running.
     */
    private boolean running = true;

    /**
     * Creates and starts a Chronos scheduling engine.
     *
     * <p>If the requested number of worker threads is not positive,
     * Chronos defaults to four worker threads.</p>
     *
     * @param threadsnum number of worker threads to create
     */
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

    /**
     * Continuously monitors the scheduling queue and dispatches
     * jobs whose execution time has been reached.
     *
     * <p>If the queue is empty, the coordinator waits until a job
     * is added. If the next job is scheduled for the future, it
     * waits until its execution time or until the queue changes.</p>
     *
     * <p>When a job becomes due, it is removed from the queue and
     * submitted to the worker executor.</p>
     *
     * <p>For jobs using {@link IntervalSchedule}, the scheduled
     * occurrence is refreshed and reinserted into the queue to
     * support recurring execution.</p>
     *
     * <p>This method runs on the dedicated coordinator thread.</p>
     */
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

                if (dueJob.getJob().getSchedule().getClass() == IntervalSchedule.class) {
                    dueJob.refresh();
                    JobQueue.add(dueJob);
                }

                executor.submit(() -> execute(readyJob));
            }
        }
    }

    /**
     * Adds a scheduled job occurrence to the scheduling queue.
     *
     * <p>The queue is protected by a lock to ensure thread-safe
     * access. After insertion, the coordinator is notified so it
     * can reevaluate the next scheduled execution.</p>
     *
     * @param sj scheduled job occurrence to enqueue
     * @throws IllegalArgumentException if the scheduled job,
     *         its underlying job, or its job identifier is null
     * @throws IllegalStateException if Chronos has already been shut down
     */
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

        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Executes the action associated with a scheduled job.
     *
     * <p>The action is invoked through its {@code Callable} interface.
     * Its execution is supervised by the timeout scheduler.</p>
     *
     * <p>If the timeout is reached before the action completes,
     * the worker thread is interrupted and the result is marked
     * as skipped.</p>
     *
     * <p>The returned result contains the action outcome and the
     * execution start and end timestamps.</p>
     *
     * @param scheduledJob scheduled occurrence to execute
     * @return the result produced by the job action, enriched with
     *         execution timestamps
     */
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
                            ExecutionState.TIME_OUT)) {
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

        System.out.println("Result: " + result);
        return result;
    }

    /**
     * Stops the Chronos scheduling engine.
     *
     * <p>The coordinator is notified and interrupted so it can
     * stop waiting for scheduled jobs. The worker executor and
     * timeout supervisor are also instructed to shut down.</p>
     *
     * <p>This method does not wait for all running jobs to finish.</p>
     */
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



