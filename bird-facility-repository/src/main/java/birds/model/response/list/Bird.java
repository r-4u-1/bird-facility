package birds.model.response.list;

import java.math.BigDecimal;

/**
 * One bird, as OUR code understands it: real types (BigDecimal, int and
 * nested records), not the strings the woods system sends.
 *
 * The service returns these to the frontend, and JSON-B turns them into JSON:
 *   {"feathers":{...},"food":{...},"id":"B-1","name":"Robin","price":250.00,...}
 * (JSON-B sorts the properties alphabetically by default.)
 */
public record Bird(
        String id,
        String name,
        String species,
        BigDecimal price,
        Feathers feathers,
        Food food) {
}
