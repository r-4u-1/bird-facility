package birds.woods.model;

import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;
import birds.woods.model.request.BirdsListRequest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the woods translation without calling woods: plain objects in, plain
 * objects out. This is what keeping the logic out of the HTTP code buys you.
 */
class BirdsListParametersTest {

    @Test
    void noPriceFilter_becomesEmptyStrings() {
        BirdsSearchCriteria criteria = new BirdsSearchCriteria("F-100", AmountRange.of(null, null));

        BirdsListParameters params = BirdsListParameters.from(criteria);

        assertEquals("", params.fromAmount());
        assertEquals("", params.toAmount());
    }

    @Test
    void priceRange_isWrittenAsPlainText() {
        BirdsSearchCriteria criteria = new BirdsSearchCriteria(
                "F-100", AmountRange.of(new BigDecimal("100.50"), null));

        BirdsListRequest request = BirdsListParameters.from(criteria).toRequest();

        assertEquals("F-100", request.body.facilityId);
        assertEquals("100.50", request.body.fromAmount);
        assertEquals("100.50", request.body.toAmount);   // copied by AmountRange.of, not here
    }
}
