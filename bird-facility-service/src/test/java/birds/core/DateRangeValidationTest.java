package birds.core;

import birds.resource.BirdsSearchRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the date rule through REAL Bean Validation (Hibernate Validator), the
 * same engine the server runs for @Valid, but with a FIXED clock: "today" is
 * always 2026-10-26 here, so the 12-month window never moves.
 *
 * That's what ClockProvider is for: DateRangeValidation asks the validation
 * context for the time instead of calling LocalDate.now() itself.
 */
class DateRangeValidationTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Stockholm");
    private static final Clock FIXED = Clock.fixed(
            LocalDate.of(2026, 10, 26).atStartOfDay(ZONE).plusHours(12).toInstant(), ZONE);

    private final Validator validator = Validation.byDefaultProvider()
            .configure()
            .clockProvider(() -> FIXED)
            .buildValidatorFactory()
            .getValidator();

    private static BirdsSearchRequest request(String from, String to) {
        BirdsSearchRequest request = new BirdsSearchRequest();
        request.setFacilityId("F-100");
        request.setFromDate(from == null ? null : LocalDate.parse(from));
        request.setToDate(to == null ? null : LocalDate.parse(to));
        return request;
    }

    /** All broken-rule messages for a request; an empty list means valid. */
    private List<String> problems(BirdsSearchRequest request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessage).sorted().toList();
    }

    private static final List<String> OUTSIDE = List.of("fromDate and toDate must be within the last 12 months");

    // ------------------------------------------------------------------ valid

    @Test
    void noDates_isValid() {
        assertEquals(List.of(), problems(request(null, null)));
    }

    @Test
    void aRangeInsideTheLastYear_isValid() {
        assertEquals(List.of(), problems(request("2026-05-01", "2026-06-30")));
    }

    @Test
    void exactlyTwelveMonthsAgoUntilToday_isValid() {
        assertEquals(List.of(), problems(request("2025-10-26", "2026-10-26")));
    }

    @Test
    void onlyFromDate_insideTheWindow_isValid() {
        assertEquals(List.of(), problems(request("2026-01-15", null)));
    }

    // ---------------------------------------------------------------- invalid

    @Test
    void fromDateOneDayTooEarly_isInvalid() {
        assertEquals(OUTSIDE, problems(request("2025-10-25", "2026-06-30")));
    }

    @Test
    void toDateInTheFuture_isInvalid() {
        assertEquals(OUTSIDE, problems(request("2026-05-01", "2026-10-27")));
    }

    @Test
    void onlyToDate_longAgo_isInvalid() {
        assertEquals(OUTSIDE, problems(request(null, "2024-01-01")));
    }

    @Test
    void reversedDates_getTheirOwnMessage() {
        assertEquals(List.of("fromDate must be on or before toDate"),
                problems(request("2026-06-30", "2026-05-01")));
    }
}
