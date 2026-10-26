package birds.core;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom Bean Validation annotation, put on the BirdsListQuery class.
 *
 * Single-field rules (@NotBlank, @Pattern) go directly on a field. A rule that
 * needs TWO fields (fromAmount <= toAmount) needs the whole object, so it is
 * an annotation on the class, and the check itself lives in BirdsValidation.
 *
 * The parts:
 *   @Constraint(validatedBy = ...)   which class runs the check
 *   @Target(TYPE)                    may only be placed on a class
 *   @Retention(RUNTIME)              kept at runtime, so the server can read it
 *   message / groups / payload       required by the Bean Validation spec
 *
 * "@interface" is how Java declares an annotation type.
 */
@Documented
@Constraint(validatedBy = BirdsValidation.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirdsQuery {

    String message() default "fromAmount must be less than or equal to toAmount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
