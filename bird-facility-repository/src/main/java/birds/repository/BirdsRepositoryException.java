package birds.repository;

/**
 * "The data source behind the repository failed."
 *
 * It lives in the shared module, next to the interface, so the service can
 * catch or map it without depending on the woods module. The service's
 * RepositoryExceptionMapper turns it into an HTTP 502.
 *
 * extends RuntimeException = UNCHECKED: callers aren't forced to catch it or
 * declare "throws". It behaves like a thrown Error in TypeScript.
 */
public class BirdsRepositoryException extends RuntimeException {

    public BirdsRepositoryException(String message) {
        super(message);             // calls the parent constructor, like super(message) in TS
    }

    public BirdsRepositoryException(String message, Throwable cause) {
        super(message, cause);      // keeps the original error for the logs
    }
}
