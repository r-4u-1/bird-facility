package birds.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The from/to rule tested in one place, with no server. Run with: mvn test
 * JUnit ~ Jest: @Test ~ test(...), assertEquals(expected, actual) ~ expect(actual).toBe(expected).
 */
class AmountRangeTest {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal FOUR_HUNDRED = new BigDecimal("400");

    @Test
    void bothMissing_meansNoFilter() {
        assertEquals(Optional.empty(), AmountRange.of(null, null));
    }

    @Test
    void onlyFrom_isCopiedIntoTo() {
        assertEquals(new AmountRange(HUNDRED, HUNDRED), AmountRange.of(HUNDRED, null).orElseThrow());
    }

    @Test
    void onlyTo_isCopiedIntoFrom() {
        assertEquals(new AmountRange(FOUR_HUNDRED, FOUR_HUNDRED), AmountRange.of(null, FOUR_HUNDRED).orElseThrow());
    }

    @Test
    void reversedRange_cannotBeCreated() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRange(FOUR_HUNDRED, HUNDRED));
    }

    @Test
    void contains_includesBothEnds() {
        AmountRange range = new AmountRange(HUNDRED, FOUR_HUNDRED);
        assertTrue(range.contains(HUNDRED));
        assertTrue(range.contains(FOUR_HUNDRED));
        assertFalse(range.contains(new BigDecimal("400.01")));
    }
}
