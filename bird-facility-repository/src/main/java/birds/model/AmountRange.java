package birds.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * A price interval that can never be invalid.
 *
 * THIS is the single home of the rule "if only one of from/to is given, use it
 * for both". The validator does not apply it, the service does not apply it,
 * and the woods code does not apply it. Everyone downstream receives an
 * AmountRange that is already complete, so nobody needs to null-check.
 *
 * "record" = an immutable data class: fields, constructor, accessors from()
 * and to(), equals, hashCode and toString are generated from this one line.
 * Like a readonly TS type with a constructor.
 *
 * BigDecimal instead of double: double can't store 0.10 exactly, which is
 * not acceptable for money.
 */
public record AmountRange(BigDecimal from, BigDecimal to) {

    /**
     * Compact constructor: runs on every "new AmountRange(...)", whoever calls it.
     * requireNonNull throws immediately with the given label as the message.
     */
    public AmountRange {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        // BigDecimal can't use > ; compareTo returns -1, 0 or 1.
        if (from.compareTo(to) > 0) {
            throw new IllegalArgumentException("from " + from + " is greater than to " + to);
        }
    }

    /**
     * Turns two optional inputs into a range:
     *
     *   from | to   | result
     *   -----+------+-----------------------------
     *   null | null | Optional.empty()  (no filter)
     *   250  | null | [250, 250]
     *   null | 400  | [400, 400]
     *   100  | 400  | [100, 400]
     *
     * "static" = called on the type: AmountRange.of(...), no instance needed.
     * Optional = a box with one value or nothing (TS: AmountRange | undefined).
     */
    public static Optional<AmountRange> of(BigDecimal from, BigDecimal to) {
        if (from == null && to == null) {
            return Optional.empty();
        }
        return Optional.of(new AmountRange(
                from != null ? from : to,     // ternary, same as in TS
                to != null ? to : from));
    }

    /** True if the amount lies inside the range, both ends included. */
    public boolean contains(BigDecimal amount) {
        return amount.compareTo(from) >= 0 && amount.compareTo(to) <= 0;
    }
}
