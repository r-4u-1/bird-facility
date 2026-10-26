package birds.error;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A request body that can't be turned into an object -> HTTP 400.
 *
 * This happens BEFORE validation: broken JSON, or a value of the wrong type
 * such as "fromDate": "2026-13-45" or "fromAmount": "abc". JSON-B fails while
 * reading the body, and the server reports that as a ProcessingException.
 * Without this mapper, Jersey would answer 500 with an HTML page; with it,
 * the caller gets the same ErrorResponse shape as for any other bad input.
 *
 * The JSON-B message goes to the log only: it names internal classes.
 */
@Provider
public class UnreadableBodyExceptionMapper implements ExceptionMapper<ProcessingException> {

    private static final Logger LOG = Logger.getLogger(UnreadableBodyExceptionMapper.class.getName());

    @Override
    public Response toResponse(ProcessingException exception) {
        LOG.log(Level.FINE, "Unreadable request body", exception);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(400, "Unreadable request body", List.of(
                        "the body must be valid JSON; amounts are numbers and dates are yyyy-MM-dd")))
                .build();
    }
}
