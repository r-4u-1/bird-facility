package birds.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * A date interval that can never be invalid: the date twin of AmountRange.
 *
 * It follows the same rule as AmountRange: if only one of from/to is given,
 * it is used for both, so the range is that single day.
 *
 * LocalDate = a calendar date with no time and no time zone ("2026-05-01").
 * Like a "YYYY-MM-DD" string in TS, but with real date maths built in.
 */
public record DateRange(LocalDate from, LocalDate to) {

    /** Compact constructor: same guarantees as AmountRange. */
    public DateRange {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from " + from + " is after to " + to);
        }
    }

    /**
     *   from       | to         | result
     *   -----------+------------+---------------------------
     *   null       | null       | Optional.empty()  (no filter)
     *   2026-05-01 | null       | [2026-05-01, 2026-05-01]
     *   null       | 2026-06-30 | [2026-06-30, 2026-06-30]
     *   2026-05-01 | 2026-06-30 | [2026-05-01, 2026-06-30]
     */
    public static Optional<DateRange> of(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return Optional.empty();
        }
        return Optional.of(new DateRange(
                from != null ? from : to,
                to != null ? to : from));
    }

    /** True if the date lies inside the range, both ends included. */
    public boolean contains(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * True if the whole range lies between "months" months before today and
     * today, both ends included. With months = 12 and today = 2026-10-26:
     *
     *   [2025-10-26, 2026-10-26]  inside
     *   [2025-10-25, 2026-01-01]  outside: from is one day too early
     *   [2026-01-01, 2026-10-27]  outside: to is in the future
     *
     * "today" is a parameter instead of LocalDate.now(), so this stays a pure
     * function: a test can pass any date and get the same answer every time.
     */
    public boolean isWithinLastMonths(int months, LocalDate today) {
        return new DateRange(today.minusMonths(months), today).contains(from)
                && !to.isAfter(today);
    }
}
