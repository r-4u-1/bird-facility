package birds.model.request;

import birds.model.AmountRange;

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
 *   - the price filter is an explicit Optional: empty means "no filter".
 *     It is never null and never an empty-string placeholder.
 */
public record BirdsSearchCriteria(String facilityId, Optional<AmountRange> priceRange) {

    public BirdsSearchCriteria {
        Objects.requireNonNull(facilityId, "facilityId");
        Objects.requireNonNull(priceRange, "priceRange (use Optional.empty())");
    }
}
