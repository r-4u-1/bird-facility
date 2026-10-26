package birds.model.response.list;

import java.util.List;

/**
 * The response body of GET /api/birds:
 *   {"birds":[ ... ],"count":2}
 *
 * Note: here the shared model doubles as the public JSON shape, which is how
 * your project's folder name (model/response/list) suggests it's used. That's
 * fine while they match. If they ever need to differ, add a separate response
 * DTO in the service module instead of changing this record.
 */
public record BirdsListResponse(int count, List<Bird> birds) {
}
