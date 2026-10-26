package woods.system.data;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Woods' "database": a fixed, in-memory list of birds per facility.
 *
 * A real lower-level system would read from a database or a mainframe. For
 * the example, a Map is enough and keeps the woods side easy to read.
 *
 * @ApplicationScoped: one shared instance, injected into the resource.
 */
@ApplicationScoped
public class BirdCatalog {

    /** How woods stores a bird internally. A nested record: only this class needs it. */
    public record StoredBird(
            String id,
            String name,
            String species,
            BigDecimal price,
            String foodType,
            int foodGramsPerDay,
            String featherType,
            int featherLengthMm,
            String primaryColor,
            String secondaryColor) {
    }

    /** facilityId -> birds. Map.of(...) creates an immutable map, like a frozen object literal. */
    private static final Map<String, List<StoredBird>> BIRDS_BY_FACILITY = Map.of(
            "F-100", List.of(
                    new StoredBird("B-1", "Robin", "Erithacus rubecula", new BigDecimal("250.00"),
                            "Insects", 12, "Contour", 65, "Brown", "Orange"),
                    new StoredBird("B-2", "Blue tit", "Cyanistes caeruleus", new BigDecimal("250.00"),
                            "Seeds", 10, "Contour", 55, "Blue", "Yellow"),
                    new StoredBird("B-3", "Magpie", "Pica pica", new BigDecimal("400.00"),
                            "Omnivore", 40, "Flight", 220, "Black", "White"),
                    new StoredBird("B-4", "Sparrow", "Passer domesticus", new BigDecimal("90.00"),
                            "Seeds", 8, "Contour", 50, "Brown", null)),
            "F-200", List.of(
                    new StoredBird("B-5", "Barn owl", "Tyto alba", new BigDecimal("1200.00"),
                            "Mice", 100, "Flight", 290, "White", "Golden"),
                    new StoredBird("B-6", "Kestrel", "Falco tinnunculus", new BigDecimal("950.00"),
                            "Mice", 70, "Flight", 250, "Chestnut", "Grey")));

    /**
     * Finds birds using woods' own rules:
     *   - an unknown facility gives an empty list (not an error)
     *   - minPrice / maxPrice may be null, meaning "no bound on that side"
     *
     * Note that woods does NOT copy one bound into the other. That rule belongs
     * to the bird facility (AmountRange.of) and was applied before the request
     * was sent. Each system keeps its own rules.
     */
    public List<StoredBird> search(String facilityId, BigDecimal minPrice, BigDecimal maxPrice) {
        return BIRDS_BY_FACILITY.getOrDefault(facilityId, List.of())
                .stream()
                .filter(b -> minPrice == null || b.price().compareTo(minPrice) >= 0)
                .filter(b -> maxPrice == null || b.price().compareTo(maxPrice) <= 0)
                .toList();
    }
}
