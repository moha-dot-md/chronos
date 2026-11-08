
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the response returned when evaluating a schedule.
 *
 * <p>A ScheduleResponse contains the result of a scheduling
 * evaluation, including the next execution date and the
 * corresponding scheduling status.</p>
 *
 * <p>If the schedule has a valid upcoming execution,
 * {@code nextDateTime} contains the corresponding date and time.
 * Otherwise, it may be {@code null}, depending on the schedule
 * implementation.</p>
 *
 * <p>Lombok generates the constructor, getters, setters,
 * {@code equals()}, {@code hashCode()}, and {@code toString()}
 * methods through {@link AllArgsConstructor} and {@link Data}.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@AllArgsConstructor
@Data
public class ScheduleResponse {

    /**
     * Date and time of the next scheduled execution.
     *
     * <p>May be {@code null} when no upcoming execution is available.</p>
     */
    private LocalDateTime nextDateTime;

    /**
     * Status describing the outcome of the scheduling evaluation.
     */
    private ScheduleStatus status;
}
