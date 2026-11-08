
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

/**
 * Represents the outcome status of a job execution.
 *
 * <p>This enumeration describes the execution result exposed
 * through {@link JobResult}.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
public enum JobStatus {

    /**
     * The job is currently being executed.
     */
    RUNNING,

    /**
     * The job execution failed.
     */
    FAILED,

    /**
     * The job execution completed successfully.
     */
    SUCCESS
}
