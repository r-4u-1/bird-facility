package birds.service;

import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;
import birds.model.response.list.BirdsListResponse;
import birds.repository.BirdsRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic. Speaks only in models: no HTTP, no JSON, no XML, no woods.
 *
 * It depends on the BirdsRepository INTERFACE. It has no idea that the birds
 * come from an XML system called woods; at runtime CDI injects
 * BirdsRepositoryImpl from the woods module.
 *
 * Today's business rule is small, "cheapest first, then by name", but this is
 * where rules like "hide birds that are reserved" or "merge two facilities"
 * belong. Not in the resource, not in the repository.
 *
 * A good test for this class: could listBirds run unchanged from a batch job
 * instead of an HTTP request? Yes, so it's free of HTTP details, as it should be.
 *
 * @ApplicationScoped = one shared instance. Safe because it keeps no
 * per-request data in fields.
 */
@ApplicationScoped
public class BirdsService {

    private final BirdsRepository birdsRepository;

    /**
     * Constructor injection: CDI calls this and passes in the repository.
     * A unit test can simply do  new BirdsService(fakeRepository).
     */
    @Inject
    public BirdsService(BirdsRepository birdsRepository) {
        this.birdsRepository = birdsRepository;   // "this." needed: the parameter has the same name
    }

    /**
     * CDI needs a no-argument constructor to build its internal proxy for
     * @ApplicationScoped beans. Our code never calls it.
     */
    protected BirdsService() {
        this(null);
    }

    public BirdsListResponse listBirds(BirdsSearchCriteria criteria) {
        List<Bird> birds = birdsRepository.findBirds(criteria)
                .stream()
                .sorted(Comparator.comparing(Bird::price)       // cheapest first...
                        .thenComparing(Bird::name))              // ...then alphabetically
                .toList();
        return new BirdsListResponse(birds.size(), birds);
    }
}
