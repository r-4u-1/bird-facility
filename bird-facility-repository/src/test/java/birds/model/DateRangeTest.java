package birds.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The date rules, tested with a FIXED "today", so the tests give the same
 * answer on every day they run.
 */
class DateRangeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 26);
    private static final LocalDate MAY_1 = LocalDate.of(2026, 5, 1);
    private static final LocalDate JUNE_30 = LocalDate.of(2026, 6, 30);

    private static DateRange range(String from, String to) {
        return new DateRange(LocalDate.parse(from), LocalDate.parse(to));
    }

    // ------------------------------------------------------------- of(...)

    @Test
    void bothMissing_meansNoFilter() {
        assertEquals(Optional.empty(), DateRange.of(null, null));
    }

    @Test
    void onlyFrom_isCopiedIntoTo() {
        assertEquals(new DateRange(MAY_1, MAY_1), DateRange.of(MAY_1, null).orElseThrow());
    }

    @Test
    void onlyTo_isCopiedIntoFrom() {
        assertEquals(new DateRange(JUNE_30, JUNE_30), DateRange.of(null, JUNE_30).orElseThrow());
    }

    @Test
    void bothGiven_areKept() {
        assertEquals(new DateRange(MAY_1, JUNE_30), DateRange.of(MAY_1, JUNE_30).orElseThrow());
    }

    @Test
    void reversedRange_cannotBeCreated() {
        assertThrows(IllegalArgumentException.class, () -> new DateRange(JUNE_30, MAY_1));
    }

    @Test
    void contains_includesBothEnds() {
        DateRange range = new DateRange(MAY_1, JUNE_30);
        assertTrue(range.contains(MAY_1));
        assertTrue(range.contains(JUNE_30));
        assertFalse(range.contains(MAY_1.minusDays(1)));
        assertFalse(range.contains(JUNE_30.plusDays(1)));
    }

    // ------------------------------------------------ inside the last 12 months

    @Test
    void exactlyTwelveMonthsBackToToday_isInside() {
        assertTrue(range("2025-10-26", "2026-10-26").isWithinLastMonths(12, TODAY));
    }

    @Test
    void aRangeInTheMiddle_isInside() {
        assertTrue(range("2026-05-01", "2026-06-30").isWithinLastMonths(12, TODAY));
    }

    @Test
    void oneDayMoreThanTwelveMonthsAgo_isOutside() {
        assertFalse(range("2025-10-25", "2026-01-01").isWithinLastMonths(12, TODAY));
    }

    @Test
    void endingTomorrow_isOutside() {
        assertFalse(range("2026-01-01", "2026-10-27").isWithinLastMonths(12, TODAY));
    }

    @Test
    void entirelyInTheFuture_isOutside() {
        assertFalse(range("2027-01-01", "2027-02-01").isWithinLastMonths(12, TODAY));
    }

    @Test
    void entirelyLongAgo_isOutside() {
        assertFalse(range("2020-01-01", "2020-12-31").isWithinLastMonths(12, TODAY));
    }

    @Test
    void leapDay_twelveMonthsBackIsTheLastDayOfFebruary() {
        // 2024-02-29 minus 12 months = 2023-02-28 (there is no 2023-02-29)
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        assertTrue(range("2023-02-28", "2024-02-29").isWithinLastMonths(12, leapDay));
        assertFalse(range("2023-02-27", "2024-02-29").isWithinLastMonths(12, leapDay));
    }
}
