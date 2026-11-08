
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys.scheduleTypes;

import chronos.sys.ScheduleResponse;
import chronos.sys.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a recurring schedule based on a fixed time interval.
 *
 * <p>An IntervalSchedule defines a first execution date and a
 * fixed duration between subsequent executions.</p>
 *
 * <p>When evaluated, it calculates the next execution date
 * strictly after the supplied reference date and time.</p>
 *
 * <p>For example, a schedule with a first execution at 10:00
 * and an interval of 30 minutes produces executions at 10:00,
 * 10:30, 11:00, and so on.</p>
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
public class IntervalSchedule implements Schedule {

    /**
     * Fixed duration between consecutive executions.
     *
     * <p>Must be a positive, non-zero duration.</p>
     */
    private final Duration interval;

    /**
     * Date and time of the first scheduled execution.
     */
    private final LocalDateTime firstExecution;

    /**
     * Calculates the next execution date strictly after
     * the supplied reference date and time.
     *
     * <p>If the first execution is still in the future,
     * it is returned directly. Otherwise, the method
     * calculates how many complete intervals have elapsed
     * and determines the next occurrence.</p>
     *
     * @param from reference date and time used to calculate
     *             the next execution
     * @return a {@link ScheduleResponse} containing the next
     *         execution date and {@link ScheduleStatus#SCHEDULED}
     */
    @Override
    public ScheduleResponse nextExecution(LocalDateTime from) {

        if (firstExecution.isAfter(from))
            return new ScheduleResponse(
                    firstExecution,
                    ScheduleStatus.SCHEDULED
            );

        Duration elapsed = Duration.between(firstExecution, from);
        long intervals = elapsed.dividedBy(interval);

        LocalDateTime nextExecution = firstExecution
                .plus(interval.multipliedBy(intervals + 1));

        return new ScheduleResponse(
                nextExecution,
                ScheduleStatus.SCHEDULED
        );
    }
}
