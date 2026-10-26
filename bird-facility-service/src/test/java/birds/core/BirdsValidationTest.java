package birds.core;

import birds.resource.BirdsListQuery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A validator is a plain class, so it can be tested with "new". The server's
 * context argument isn't used, so null is fine here.
 */
class BirdsValidationTest {

    private final BirdsValidation validation = new BirdsValidation();

    private static BirdsListQuery query(String from, String to) {
        BirdsListQuery query = new BirdsListQuery();
        query.setFacilityId("F-100");
        query.setFromAmount(from);
        query.setToAmount(to);
        return query;
    }

    @Test
    void reversedRange_isInvalid() {
        assertFalse(validation.isValid(query("500", "100"), null));
    }

    @Test
    void onlyOneAmount_isValid_andIsNotChanged() {
        BirdsListQuery query = query("250", null);
        assertTrue(validation.isValid(query, null));
        // The validator must not fill in toAmount: that's AmountRange.of's job.
        assertNull(query.toAmountAsDecimal());
    }

    @Test
    void blankAmounts_areValid_becauseBlankMeansNotSent() {
        assertTrue(validation.isValid(query("", "100"), null));
        assertTrue(validation.isValid(query("500", "  "), null));
    }

    @Test
    void badNumber_isLeftToPattern() {
        assertTrue(validation.isValid(query("abc", "100"), null));
    }
}
