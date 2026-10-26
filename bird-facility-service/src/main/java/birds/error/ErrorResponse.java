package birds.error;

import java.util.List;

/**
 * One JSON shape for every error, so the frontend always knows what to expect:
 *   {"details":["facilityId is required"],"error":"Validation failed","status":400}
 */
public record ErrorResponse(int status, String error, List<String> details) {
}
