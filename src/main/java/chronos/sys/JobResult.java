
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the result of a job execution in the Chronos scheduling engine.
 *
 * <p>A JobResult stores the execution status, standard output,
 * error information, and timestamps indicating when the execution
 * started and ended.</p>
 *
 * <p>The job action is responsible for producing the result content,
 * while Chronos records the execution timestamps.</p>
 *
 * <p>Lombok generates the getters, setters, {@code equals()},
 * {@code hashCode()}, and other utility methods through
 * {@link Data}.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class JobResult {

    /**
     * Final status of the job execution.
     */
    private JobStatus status;

    /**
     * Standard output or other textual information produced
     * by the job action.
     */
    private String output;

    /**
     * Error information produced when the job execution fails
     * or cannot complete successfully.
     */
    private String error;

    /**
     * Date and time at which the job execution started.
     */
    private LocalDateTime startPeriod;

    /**
     * Date and time at which the job execution ended.
     */
    private LocalDateTime endPeriod;
    

}
