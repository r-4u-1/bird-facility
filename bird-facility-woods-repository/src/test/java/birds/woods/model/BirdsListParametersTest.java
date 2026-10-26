package birds.woods.model;

import birds.model.AmountRange;
import birds.model.DateRange;
import birds.model.request.BirdsSearchCriteria;
import birds.woods.model.request.BirdsListRequest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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

    @Test
    void noArrivalFilter_becomesEmptyStrings() {
        BirdsSearchCriteria criteria = new BirdsSearchCriteria("F-100", AmountRange.of(null, null));

        BirdsListParameters params = BirdsListParameters.from(criteria);

        assertEquals("", params.fromDate());
        assertEquals("", params.toDate());
    }

    @Test
    void arrivalRange_isWrittenAsIsoDates() {
        BirdsSearchCriteria criteria = new BirdsSearchCriteria("F-100", Optional.empty(),
                DateRange.of(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30)));

        BirdsListRequest request = BirdsListParameters.from(criteria).toRequest();

        assertEquals("2026-05-01", request.body.fromDate);
        assertEquals("2026-06-30", request.body.toDate);
    }
}
