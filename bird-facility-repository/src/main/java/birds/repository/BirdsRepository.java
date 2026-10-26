package birds.repository;

import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;

import java.util.List;

/**
 * The contract for "get birds from somewhere". An interface = method
 * signatures with no code, exactly like a TypeScript interface.
 *
 * Who uses it:
 *   - BirdsService (service module) CALLS it, without knowing who implements it.
 *   - BirdsRepositoryImpl (woods module) IMPLEMENTS it by calling woods.
 *
 * At runtime CDI sees that BirdsService asks for a BirdsRepository, finds the
 * one class that implements it (BirdsRepositoryImpl) and injects that.
 *
 * Because this interface has a single method, a unit test can fake it with a
 * lambda:  BirdsRepository fake = criteria -> List.of(robin);
 */
public interface BirdsRepository {

    /**
     * Returns the birds in a facility that match the criteria.
     *
     * @throws BirdsRepositoryException when the data source can't be reached
     *         or answers with an error
     */
    List<Bird> findBirds(BirdsSearchCriteria criteria);
}
