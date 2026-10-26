package birds.resource;

import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Plain JUnit: the normalisation of blank and missing amounts, tested on the
 * DTO alone. No mocks, no server.
 *
 * Rule under test (BirdsListQuery.toCriteria + AmountRange.of):
 *   - blank ("" or spaces) means the same as "not sent"
 *   - if only one side is given, it is used for both
 *   - if neither is given, there is no price filter
 */
class BirdsListQueryTest {

    private static BirdsListQuery query(String from, String to) {
        BirdsListQuery query = new BirdsListQuery();
        query.setFacilityId("F-100");
        query.setFromAmount(from);
        query.setToAmount(to);
        return query;
    }

    private static Optional<AmountRange> range(String from, String to) {
        return Optional.of(new AmountRange(new BigDecimal(from), new BigDecimal(to)));
    }

    /**
     * @ParameterizedTest runs the same test once per row, like Jest's test.each.
     *
     * In @CsvSource:  ''  = an empty string,  '  ' = two spaces,
     *                 an empty cell (nothing between commas) = null.
     */
    @ParameterizedTest(name = "from=[{0}] to=[{1}] -> [{2}, {3}]")
    @CsvSource({
            // from,  to,    expectedFrom, expectedTo
            "'',      300,   300,          300",    // from blank       -> use to for both
            "'  ',    300,   300,          300",    // from only spaces -> same as blank
            ",        300,   300,          300",    // from not sent    -> same result
            "250,     '',    250,          250",    // to blank         -> use from for both
            "250,     '  ',  250,          250",
            "250,     ,      250,          250",    // to not sent
            "100,     300,   100,          300",    // both given       -> kept as they are
            "' 100 ', 300,   100,          300"     // spaces around a number are trimmed
    })
    void oneSideBlankOrMissing_isNormalised(String from, String to, String expectedFrom, String expectedTo) {
        BirdsSearchCriteria criteria = query(from, to).toCriteria();

        assertEquals(range(expectedFrom, expectedTo), criteria.priceRange());
    }

    @ParameterizedTest(name = "from=[{0}] to=[{1}] -> no filter")
    @CsvSource({
            "'',   ''",      // both blank
            "'  ', ' '",     // both only spaces
            ",     ",        // both not sent
            "'',   "         // one blank, one not sent
    })
    void bothBlankOrMissing_meansNoPriceFilter(String from, String to) {
        assertEquals(Optional.empty(), query(from, to).toCriteria().priceRange());
    }

    @Test
    void facilityId_isPassedThrough() {
        assertEquals("F-100", query(null, null).toCriteria().facilityId());
    }
}
