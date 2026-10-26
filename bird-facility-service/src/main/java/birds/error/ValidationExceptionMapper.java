package birds.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

/**
 * Turns failed validation into HTTP 400 with every broken rule listed.
 *
 * Express equivalent: an error-handling middleware
 *   app.use((err, req, res, next) => { if (err instanceof ZodError) res.status(400).json(...) })
 *
 * When @Valid fails in BirdsApi, the server throws ConstraintViolationException
 * BEFORE our method runs. JAX-RS then looks for an ExceptionMapper whose type
 * matches that exception, and finds this one.
 *
 * @Provider = "register me automatically". No other wiring is needed.
 * ExceptionMapper<ConstraintViolationException> = "I handle this exception type".
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> messages = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)   // the message="..." text from each annotation
                .sorted()                               // stable order, easier to test
                .toList();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(400, "Validation failed", messages))
                .build();
    }
}
