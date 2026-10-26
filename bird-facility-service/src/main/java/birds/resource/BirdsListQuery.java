package birds.resource;

import birds.core.ValidBirdsQuery;
import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.QueryParam;

import java.math.BigDecimal;

/**
 * The query parameters of GET /api/birds, plus the rules for what is valid.
 * (Request DTO = the public contract with the frontend.)
 *
 *   GET /api/birds?facilityId=F-100&fromAmount=100&toAmount=300
 *
 * Two kinds of annotations:
 *   - JAX-RS (@QueryParam): WHERE a value comes from, like req.query.facilityId
 *   - Bean Validation (@NotBlank, @Pattern, @ValidBirdsQuery): WHAT is valid,
 *     like a zod schema
 *
 * A parameter that wasn't sent stays null. We deliberately do NOT default it
 * to "": the empty string is woods' business, not ours.
 *
 * NORMALISATION OF BLANK AMOUNTS: a frontend often sends "fromAmount=" (empty)
 * or "fromAmount=%20" (a space) when a field is left empty. We decide that a
 * blank amount means the same as "not sent", and we normalise that exactly
 * once, here at the edge (toDecimal below). From toCriteria() onwards nobody
 * ever sees a blank string, only null -> Optional.
 *
 * Amounts arrive as String, not BigDecimal: if JAX-RS fails to convert "abc"
 * into a BigDecimal, the spec says it answers 404, which confuses clients.
 * As a String it always arrives, and @Pattern gives a proper 400 instead.
 *
 * In your real project this class may be GENERATED from api-contract-birds.yaml.
 */
@ValidBirdsQuery    // the cross-field rule (from <= to), checked by core/BirdsValidation
public class BirdsListQuery {

    /**
     * Blank (empty or only spaces), OR up to 13 digits with optionally 1-2
     * decimals, spaces around it allowed: "", " ", "250", " 250.50 ".
     * Letting blank and padded values through validation is what allows them
     * to be normalised in toDecimal instead of rejected.
     */
    static final String AMOUNT_FORMAT = "\\s*|\\s*\\d{1,13}(\\.\\d{1,2})?\\s*";

    @QueryParam("facilityId")
    @NotBlank(message = "facilityId is required")
    @Pattern(regexp = "F-\\d{3}", message = "facilityId must look like F-100")
    private String facilityId;

    @QueryParam("fromAmount")
    @Pattern(regexp = AMOUNT_FORMAT, message = "fromAmount must be a number with at most 2 decimals")
    private String fromAmount;      // null passes @Pattern: optional

    @QueryParam("toAmount")
    @Pattern(regexp = AMOUNT_FORMAT, message = "toAmount must be a number with at most 2 decimals")
    private String toAmount;

    /**
     * DTO -> clean model. The ONE conversion step, called after validation.
     * The from/to rule itself is applied by AmountRange.of, not here.
     */
    public BirdsSearchCriteria toCriteria() {
        return new BirdsSearchCriteria(
                facilityId,
                AmountRange.of(fromAmountAsDecimal(), toAmountAsDecimal()));
    }

    /** Parsed amounts. Shared with BirdsValidation so parsing exists once. */
    public BigDecimal fromAmountAsDecimal() {
        return toDecimal(fromAmount);
    }

    public BigDecimal toAmountAsDecimal() {
        return toDecimal(toAmount);
    }

    /** The normalisation: null or blank -> null ("not sent"), otherwise the number. */
    private static BigDecimal toDecimal(String value) {
        return (value == null || value.isBlank()) ? null : new BigDecimal(value.trim());
    }

    // Setters are for unit tests. At runtime JAX-RS writes straight into the fields.
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }
    public void setFromAmount(String fromAmount) { this.fromAmount = fromAmount; }
    public void setToAmount(String toAmount)     { this.toAmount = toAmount; }
}
