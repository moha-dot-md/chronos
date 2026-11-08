
/*
 * Copyright (c) 2026 Lemine
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for details.
 */

package chronos.sys;

import lombok.Getter;

/**
 * Defines the priority levels available for Chronos jobs.
 *
 * <p>Priority determines the execution order of jobs when
 * multiple scheduled jobs have the same execution date and time.</p>
 *
 * <p>Each priority level is associated with an integer value.
 * A lower value represents a higher priority.</p>
 *
 * <p>For example, {@code HIGH} (1) is ordered before
 * {@code LOW} (2).</p>
 *
 * @author Lemine
 * @since 1.0.0
 */
@Getter
public enum Priority {

    /**
     * Lowest execution priority.
     */
    LOW(2),

    /**
     * Highest execution priority.
     */
    HIGH(1);

    /**
     * Integer value associated with this priority level.
     *
     * <p>Used by the scheduling queue comparator to order jobs.</p>
     */
    private final int value;

    /**
     * Creates a priority level with its associated ordering value.
     *
     * @param value integer used to determine priority ordering
     */
    Priority(int value) {
        this.value = value;
    }
}
