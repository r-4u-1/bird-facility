package birds.core;

import java.math.BigDecimal;

/**
 * Anything that carries an optional fromAmount and toAmount: the GET query
 * (BirdsListQuery) and the POST body (BirdsSearchRequest).
 *
 * BirdsValidation checks this interface instead of one concrete class, so the
 * "from <= to" rule exists once and both endpoints use it. Like a TS
 * interface that two different request types both satisfy.
 */
public interface AmountQuery {

    /** The lowest price, or null if not sent. */
    BigDecimal fromAmountAsDecimal();

    /** The highest price, or null if not sent. */
    BigDecimal toAmountAsDecimal();
}
