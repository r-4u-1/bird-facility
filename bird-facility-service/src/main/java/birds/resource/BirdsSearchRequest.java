package birds.resource;

import birds.core.AmountQuery;
import birds.core.ValidBirdsQuery;
import birds.core.ValidDateRange;
import birds.model.AmountRange;
import birds.model.DateRange;
import birds.model.request.BirdsSearchCriteria;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The JSON body of POST /api/birds/search. (Request DTO, like BirdsListQuery.)
 *
 *   {
 *     "facilityId": "F-100",
 *     "fromAmount": 100,           optional
 *     "toAmount":   300,           optional
 *     "fromDate":   "2026-05-01",  optional, yyyy-MM-dd
 *     "toDate":     "2026-06-30"   optional, yyyy-MM-dd
 *   }
 *
 * Unlike the GET query, a JSON body has real types: JSON-B turns 100 into a
 * BigDecimal and "2026-05-01" into a LocalDate for us. A field that is missing
 * (or null) in the JSON stays null here, meaning "not sent".
 *
 * JSON-B fills the object through the setters, the way JAX-RS fills
 * BirdsListQuery through its fields.
 *
 * The rules, all checked by @Valid before BirdsApi.searchBirds runs:
 *   - facilityId: same as GET
 *   - amounts: not negative, at most 2 decimals, from <= to (@ValidBirdsQuery)
 *   - dates: from <= to, both within the last 12 months (@ValidDateRange)
 */
@ValidBirdsQuery
@ValidDateRange
public class BirdsSearchRequest implements AmountQuery {

    @NotBlank(message = "facilityId is required")
    @Pattern(regexp = "F-\\d{3}", message = "facilityId must look like F-100")
    private String facilityId;

    @DecimalMin(value = "0", message = "fromAmount must not be negative")
    @Digits(integer = 13, fraction = 2, message = "fromAmount must be a number with at most 2 decimals")
    private BigDecimal fromAmount;

    @DecimalMin(value = "0", message = "toAmount must not be negative")
    @Digits(integer = 13, fraction = 2, message = "toAmount must be a number with at most 2 decimals")
    private BigDecimal toAmount;

    private LocalDate fromDate;
    private LocalDate toDate;

    /** DTO -> clean model, after validation. The from/to rules are applied by AmountRange.of and DateRange.of. */
    public BirdsSearchCriteria toCriteria() {
        return new BirdsSearchCriteria(
                facilityId,
                AmountRange.of(fromAmount, toAmount),
                DateRange.of(fromDate, toDate));
    }

    @Override
    public BigDecimal fromAmountAsDecimal() {
        return fromAmount;
    }

    @Override
    public BigDecimal toAmountAsDecimal() {
        return toAmount;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate()   { return toDate; }

    // Setters: used by JSON-B at runtime, and by unit tests.
    public void setFacilityId(String facilityId)   { this.facilityId = facilityId; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }
    public void setToAmount(BigDecimal toAmount)     { this.toAmount = toAmount; }
    public void setFromDate(LocalDate fromDate)    { this.fromDate = fromDate; }
    public void setToDate(LocalDate toDate)        { this.toDate = toDate; }
}
