package birds.woods.model;

import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;
import birds.woods.model.request.BirdsListBody;
import birds.woods.model.request.BirdsListRequest;

import java.math.BigDecimal;

/**
 * Our search criteria, translated into WOODS' way of saying it.
 *
 * This class has one clear job, and it's the answer to "where does the empty
 * string go?": woods wants "" when there is no filter. That's a quirk of the
 * woods format, NOT a default for our data, so it's applied here, at the last
 * moment before the XML is built, and nowhere else.
 *
 * What it must NOT do: decide what a missing from/to means. That already
 * happened in AmountRange.of(...). By the time criteria arrive here, a price
 * range is either complete [from, to] or absent.
 */
public record BirdsListParameters(String facilityId, String fromAmount, String toAmount) {

    /** How woods spells "no filter". */
    public static final String NO_FILTER = "";

    /**
     * Our clean criteria -> woods-format strings.
     *
     * criteria.priceRange() is an Optional<AmountRange>. Reading the chain:
     *   .map(AmountRange::from)        if there is a range, take its from
     *   .map(BigDecimal::toPlainString) turn it into text ("100.50", never "1.005E+2")
     *   .orElse(NO_FILTER)             if there was no range, use ""
     */
    public static BirdsListParameters from(BirdsSearchCriteria criteria) {
        return new BirdsListParameters(
                criteria.facilityId(),
                criteria.priceRange().map(AmountRange::from).map(BigDecimal::toPlainString).orElse(NO_FILTER),
                criteria.priceRange().map(AmountRange::to).map(BigDecimal::toPlainString).orElse(NO_FILTER));
    }

    /** Builds the JAXB envelope: <BirdsListRequest><Body>...</Body></BirdsListRequest>. */
    public BirdsListRequest toRequest() {
        BirdsListBody body = new BirdsListBody();
        body.facilityId = facilityId;
        body.fromAmount = fromAmount;
        body.toAmount = toAmount;

        BirdsListRequest request = new BirdsListRequest();
        request.body = body;
        return request;
    }
}
