package birds.model.request;

import birds.model.AmountRange;
import birds.model.DateRange;

import java.util.Objects;
import java.util.Optional;

/**
 * "What to search for", in clean form. This is what the service hands to the
 * repository.
 *
 * It lives in THIS shared module because the BirdsRepository interface uses
 * it, and the service and the woods module both need to see that interface.
 *
 * Compared with the raw query parameters, this is already normalised:
 *   - facilityId is never null
 *   - the price and arrival filters are explicit Optionals: empty means
 *     "no filter". They are never null and never an empty-string placeholder.
 */
public record BirdsSearchCriteria(
        String facilityId,
        Optional<AmountRange> priceRange,
        Optional<DateRange> arrivalRange) {

    public BirdsSearchCriteria {
        Objects.requireNonNull(facilityId, "facilityId");
        Objects.requireNonNull(priceRange, "priceRange (use Optional.empty())");
        Objects.requireNonNull(arrivalRange, "arrivalRange (use Optional.empty())");
    }

    /** For searches without an arrival filter, such as GET /api/birds. */
    public BirdsSearchCriteria(String facilityId, Optional<AmountRange> priceRange) {
        this(facilityId, priceRange, Optional.empty());
    }
}
