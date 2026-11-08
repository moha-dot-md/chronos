
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys.jobtypes;

import chronos.sys.ScheduleResponse;
import chronos.sys.scheduleTypes.IntervalSchedule;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Represents a scheduled occurrence of a {@link Job}.
 *
 * <p>Unlike {@link Job}, which describes the action and its
 * scheduling configuration, ScheduledJob represents a concrete
 * execution occurrence with a calculated execution date and time.</p>
 *
 * <p>When instantiated, it evaluates the job's schedule to determine
 * its next execution date.</p>
 *
 * <p>For recurring jobs using {@link IntervalSchedule}, the
 * {@link #refresh()} method calculates the subsequent occurrence
 * after the current scheduled execution.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
public class ScheduledJob {

    /**
     * Job definition associated with this scheduled occurrence.
     */
    @Getter
    private final Job job;

    /**
     * Date and time at which this occurrence is scheduled to execute.
     *
     * <p>May be {@code null} if the schedule does not provide
     * a valid upcoming execution date.</p>
     */
    @Getter
    private LocalDateTime executionDateTime;

    /**
     * Creates a scheduled job occurrence and calculates its
     * next execution date.
     *
     * <p>The job's schedule is evaluated using the current
     * date and time as the reference.</p>
     *
     * @param job job definition from which the scheduled occurrence
     *            is created
     */
    public ScheduledJob(Job job) {
        this.job = job;

        ScheduleResponse response =
                job.getSchedule().nextExecution(LocalDateTime.now());

        this.executionDateTime =
                response != null ? response.getNextDateTime() : null;
    }

    /**
     * Refreshes the execution date of this scheduled occurrence.
     *
     * <p>For jobs using {@link IntervalSchedule}, the next
     * execution date is calculated using the current scheduled
     * execution date as the reference.</p>
     *
     * <p>For other schedule implementations, this method
     * does not modify the execution date.</p>
     */
    public void refresh() {

        if (this.job.getSchedule().getClass() == IntervalSchedule.class) {

            ScheduleResponse resp =
                    this.job.getSchedule().nextExecution(executionDateTime);

            this.executionDateTime =
                    resp != null ? resp.getNextDateTime() : null;
        }

        return;
    }
}
