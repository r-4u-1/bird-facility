package birds.core;

import birds.resource.BirdsListQuery;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * The check behind @ValidBirdsQuery: fromAmount must not be greater than toAmount.
 *
 * A validator answers ONE question: "is this input acceptable?" It returns
 * true or false and nothing else. It must NOT:
 *   - set default values (no "" for missing amounts)
 *   - copy fromAmount into toAmount
 *   - change the object in any way
 * Doing any of that here is exactly how the same rule ends up in two places:
 * the validator "fixes" the data, and later code checks again because it
 * can't be sure the validator ran first.
 *
 * ConstraintValidator<ValidBirdsQuery, BirdsListQuery> reads as: "validates
 * the @ValidBirdsQuery annotation, when it's placed on a BirdsListQuery".
 * The server creates this class itself; you never call it directly.
 */
public class BirdsValidation implements ConstraintValidator<ValidBirdsQuery, BirdsListQuery> {

    @Override
    public boolean isValid(BirdsListQuery query, ConstraintValidatorContext context) {
        if (query == null) {
            return true;    // convention: null is @NotNull's job, not every validator's
        }

        BigDecimal from;
        BigDecimal to;
        try {
            from = query.fromAmountAsDecimal();
            to = query.toAmountAsDecimal();
        } catch (NumberFormatException e) {
            return true;    // a badly formatted number is already reported by @Pattern; don't report it twice
        }

        // Only a rule when BOTH are present. With one or none there is no order
        // to get wrong. What a missing value MEANS is decided by AmountRange.of,
        // not here. Looking at null to decide "does my check apply?" is a guard,
        // not a second copy of the rule.
        return from == null || to == null || from.compareTo(to) <= 0;
    }
}
