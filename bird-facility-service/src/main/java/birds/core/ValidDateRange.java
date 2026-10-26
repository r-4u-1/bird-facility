package birds.core;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level rule for fromDate / toDate, checked by DateRangeValidation:
 *   - fromDate must be on or before toDate
 *   - both must lie inside the last 12 months (today included, no future)
 *
 * Built the same way as @ValidBirdsQuery; see that file for what each part means.
 */
@Documented
@Constraint(validatedBy = DateRangeValidation.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "fromDate and toDate must be within the last 12 months";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
