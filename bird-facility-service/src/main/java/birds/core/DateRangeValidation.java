package birds.core;

import birds.model.DateRange;
import birds.resource.BirdsSearchRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * The check behind @ValidDateRange: verifies the two dates from the POST body.
 *
 * Same contract as BirdsValidation: answer true or false, never change the
 * request. What a missing date MEANS is decided by DateRange.of, and the
 * "inside the last N months" maths lives in DateRange.isWithinLastMonths.
 * This class only connects the request to those rules.
 *
 * Where "today" comes from: context.getClockProvider() is Bean Validation's
 * standard clock. On the server it is the system clock; a test can configure
 * a fixed one, so the 12-month window doesn't move while tests run.
 */
public class DateRangeValidation implements ConstraintValidator<ValidDateRange, BirdsSearchRequest> {

    /** How far back a search may go. */
    public static final int MONTHS_BACK = 12;

    @Override
    public boolean isValid(BirdsSearchRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;    // null is @NotNull's job
        }
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();

        if (from == null && to == null) {
            return true;    // no date filter: nothing to check
        }
        if (from != null && to != null && from.isAfter(to)) {
            return reject(context, "fromDate must be on or before toDate");
        }

        LocalDate today = LocalDate.now(context.getClockProvider().getClock());
        boolean inside = DateRange.of(from, to).orElseThrow().isWithinLastMonths(MONTHS_BACK, today);
        return inside || reject(context, "fromDate and toDate must be within the last 12 months");
    }

    /**
     * Replaces the annotation's single default message with a specific one,
     * so the 400 response says exactly which rule was broken.
     */
    private static boolean reject(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
