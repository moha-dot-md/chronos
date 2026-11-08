
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

/**
 * Represents the scheduling status returned when evaluating
 * a job's schedule.
 *
 * <p>This enumeration indicates whether a schedule provides
 * an upcoming execution date or whether no execution is
 * currently scheduled.</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
public enum ScheduleStatus {

    /**
     * An upcoming execution has been successfully scheduled.
     */
    SCHEDULED,

    /**
     * No upcoming execution is available for the evaluated schedule.
     */
    NOT_SCHEDULED
}
