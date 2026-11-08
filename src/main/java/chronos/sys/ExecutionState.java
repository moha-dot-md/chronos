
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

/**
 * Represents the lifecycle state of a job execution.
 *
 * <p>Chronos uses this enumeration to track whether a job is
 * currently executing, has completed its execution, or has
 * exceeded its configured timeout.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
public enum ExecutionState {

    /**
     * The job is currently being executed.
     */
    RUNNING,

    /**
     * The job execution has completed.
     */
    COMPLETED,

    /**
     * The job execution has exceeded its configured timeout.
     */
    TIME_OUT
}
