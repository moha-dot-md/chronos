
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys.jobtypes;

import chronos.sys.JobResult;
import chronos.sys.Priority;
import chronos.sys.scheduleTypes.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Represents a job definition managed by the Chronos scheduling engine.
 *
 * <p>A Job encapsulates the information required to schedule and
 * execute an action. It defines what should be executed, when it
 * should be executed, and under which execution constraints.</p>
 *
 * <p>A job consists of:</p>
 * <ul>
 *   <li>A unique identifier and a descriptive name.</li>
 *   <li>A {@link Callable} action returning a {@link JobResult}.</li>
 *   <li>A {@link Schedule} defining its execution timing.</li>
 *   <li>A timeout limiting its execution duration.</li>
 *   <li>A {@link Priority} determining its scheduling precedence.</li>
 * </ul>
 *
 * <p>Lombok generates constructors, getters, setters, a builder,
 * and utility methods through the annotations declared on this class.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Job {

    /**
     * Unique identifier used to distinguish this job.
     */
    private String id;

    /**
     * Human-readable name describing the job.
     */
    private String name;

    /**
     * Action executed by Chronos when the job becomes due.
     *
     * <p>The action is represented by a {@link Callable} that
     * returns a {@link JobResult} and may throw an exception
     * during execution.</p>
     */
    private Callable<JobResult> action;

    /**
     * Result associated with this job.
     *
     * <p>This field represents the result reference stored
     * directly in the job definition.</p>
     */
    private JobResult result;

    /**
     * Scheduling strategy defining when this job should execute.
     */
    private Schedule schedule;

    /**
     * Maximum permitted execution duration before the job
     * is considered to have exceeded its timeout.
     */
    private Duration timeOut;

    /**
     * Priority assigned to this job.
     *
     * <p>Used by Chronos to order jobs sharing the same
     * scheduled execution date and time.</p>
     */
    private Priority priority;
}
