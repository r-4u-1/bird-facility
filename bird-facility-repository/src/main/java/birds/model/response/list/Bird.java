package birds.model.response.list;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One bird, as OUR code understands it: real types (BigDecimal, LocalDate,
 * int and nested records), not the strings the woods system sends.
 *
 * The service returns these to the frontend, and JSON-B turns them into JSON:
 *   {"arrivedOn":"2026-05-01","feathers":{...},"food":{...},"id":"B-1",...}
 * (JSON-B sorts the properties alphabetically by default, and writes a
 * LocalDate as "yyyy-MM-dd".)
 *
 * arrivedOn = the day the bird arrived at the facility. May be null if woods
 * doesn't know it.
 */
public record Bird(
        String id,
        String name,
        String species,
        BigDecimal price,
        Feathers feathers,
        Food food,
        LocalDate arrivedOn) {
}
