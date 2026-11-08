
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

import java.time.LocalDateTime;

/**
 * Represents a schedule that executes a job only once
 * at a specific date and time.
 *
 * <p>When evaluated, the schedule checks whether its execution
 * date is strictly after the supplied reference date and time.</p>
 *
 * <p>If the execution date is still in the future, the schedule
 * returns it with the {@link ScheduleStatus#SCHEDULED} status.
 * Otherwise, it returns {@code null} as the next execution date
 * with the {@link ScheduleStatus#NOT_SCHEDULED} status.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@AllArgsConstructor
public class OneTimeSchedule implements Schedule {

    /**
     * Date and time at which the job should be executed.
     */
    private final LocalDateTime executionDate;

    /**
     * Evaluates the schedule and determines whether its single
     * execution is still upcoming.
     *
     * @param from reference date and time used to evaluate
     *             the schedule
     * @return a {@link ScheduleResponse} containing the execution
     *         date and {@link ScheduleStatus#SCHEDULED} if the
     *         execution is still upcoming; otherwise, a response
     *         with a {@code null} date and
     *         {@link ScheduleStatus#NOT_SCHEDULED}
     */
    @Override
    public ScheduleResponse nextExecution(LocalDateTime from) {

        if (executionDate.isAfter(from)) {
            return new ScheduleResponse(
                    executionDate,
                    ScheduleStatus.SCHEDULED
            );
        }

        return new ScheduleResponse(
                null,
                ScheduleStatus.NOT_SCHEDULED
        );
    }
}
