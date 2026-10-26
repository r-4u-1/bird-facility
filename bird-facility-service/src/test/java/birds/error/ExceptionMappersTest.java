package birds.error;

import birds.repository.BirdsRepositoryException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The exception mappers called directly: exception in, Response out.
 *
 * Response.status(...) needs a JAX-RS implementation at runtime; on the server
 * that's the server's, here it's Jersey from the test dependencies.
 *
 * getEntity() returns the Java object BEFORE it is turned into JSON, so we can
 * compare it with a plain record equals. BirdsApiJerseyTest checks the JSON.
 */
class ExceptionMappersTest {

    @Test
    void woodsDown_becomes502_withoutLeakingDetails() {
        BirdsRepositoryException woodsDown = new BirdsRepositoryException(
                "Could not call woods at http://woods.internal:8080", new ConnectException("Connection refused"));

        Response response = new RepositoryExceptionMapper().toResponse(woodsDown);

        assertEquals(502, response.getStatus());
        // Only the safe message goes to the caller; host names stay in the log.
        assertEquals(new ErrorResponse(502, "Bird data is unavailable right now", List.of()),
                response.getEntity());
    }

    @Test
    @SuppressWarnings("unchecked")   // mock(ConstraintViolation.class) has no generic type
    void validationErrors_become400_withEveryMessageSorted() {
        // Mockito fakes the two violations the validator would have produced.
        ConstraintViolation<Object> second = mock(ConstraintViolation.class);
        when(second.getMessage()).thenReturn("toAmount must be a number with at most 2 decimals");
        ConstraintViolation<Object> first = mock(ConstraintViolation.class);
        when(first.getMessage()).thenReturn("facilityId is required");

        Response response = new ValidationExceptionMapper()
                .toResponse(new ConstraintViolationException(Set.of(second, first)));

        assertEquals(400, response.getStatus());
        assertEquals(new ErrorResponse(400, "Validation failed", List.of(
                        "facilityId is required",
                        "toAmount must be a number with at most 2 decimals")),
                response.getEntity());
    }
}
