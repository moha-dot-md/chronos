
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys.scheduleTypes;

import chronos.sys.ScheduleResponse;

import java.time.LocalDateTime;

/**
 * Defines the contract for schedule implementations in Chronos.
 *
 * <p>A Schedule determines the next execution date of a job
 * based on a reference date and time.</p>
 *
 * <p>Implementations are responsible for calculating the next
 * occurrence according to their scheduling strategy.</p>
 *
 * <p>For example:</p>
 * <ul>
 *   <li>{@link OneTimeSchedule} represents a single execution.</li>
 *   <li>{@link IntervalSchedule} represents recurring executions
 *       separated by a fixed duration.</li>
 * </ul>
 *
 * <p>This interface is annotated with {@link FunctionalInterface},
 * allowing schedule implementations to be expressed using lambda
 * expressions.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@FunctionalInterface
public interface Schedule {

    /**
     * Calculates the next scheduled execution based on a reference
     * date and time.
     *
     * @param from reference date and time from which the next
     *             execution should be calculated
     * @return a {@link ScheduleResponse} describing the next
     *         execution date and scheduling status
     */
    ScheduleResponse nextExecution(LocalDateTime from);
}
