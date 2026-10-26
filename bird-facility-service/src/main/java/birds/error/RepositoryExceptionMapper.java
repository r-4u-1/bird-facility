package birds.error;

import birds.repository.BirdsRepositoryException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The data source failed -> HTTP 502 Bad Gateway ("the system behind me failed",
 * as opposed to 500 "I failed").
 *
 * The repository only throws BirdsRepositoryException; it doesn't know about
 * our HTTP status codes. Deciding "this means 502" happens here, in one place.
 * The technical details go to the log, and the caller gets a safe message.
 */
@Provider
public class RepositoryExceptionMapper implements ExceptionMapper<BirdsRepositoryException> {

    private static final Logger LOG = Logger.getLogger(RepositoryExceptionMapper.class.getName());

    @Override
    public Response toResponse(BirdsRepositoryException exception) {
        LOG.log(Level.WARNING, "Bird data source failed", exception);
        return Response.status(Response.Status.BAD_GATEWAY)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(502, "Bird data is unavailable right now", List.of()))
                .build();
    }
}
