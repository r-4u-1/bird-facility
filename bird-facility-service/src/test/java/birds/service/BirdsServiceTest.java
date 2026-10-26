package birds.service;

import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;
import birds.model.response.list.BirdsListResponse;
import birds.repository.BirdsRepository;
import birds.repository.BirdsRepositoryException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the service WITHOUT woods, without a server and without CDI.
 *
 * Two ways to fake the repository are shown:
 *   1. a lambda: BirdsRepository has one method, so a lambda is a complete
 *      implementation. In TS:  const fake: BirdsRepository = { findBirds: () => [...] }
 *   2. Mockito: mock(...) + when(...) + verify(...), useful when you also want
 *      to check HOW the repository was called, or make it throw.
 */
class BirdsServiceTest {

    private static Bird bird(String name, String price) {
        return new Bird("B-" + name, name, "test species", new BigDecimal(price), null, null, null);
    }

    private static BirdsSearchCriteria criteria() {
        return new BirdsSearchCriteria("F-100", AmountRange.of(new BigDecimal("250"), null));
    }

    // ------------------------------------------------------------ 1. lambda fake

    @Test
    void listBirds_sortsCheapestFirstThenByName() {
        BirdsRepository fake = criteria -> List.of(
                bird("Magpie", "400"),
                bird("Robin", "250"),
                bird("Blue tit", "250"));
        BirdsService service = new BirdsService(fake);   // constructor injection, by hand

        BirdsListResponse response = service.listBirds(
                new BirdsSearchCriteria("F-100", AmountRange.of(null, null)));

        assertEquals(3, response.count());
        assertEquals(List.of("Blue tit", "Robin", "Magpie"),
                response.birds().stream().map(Bird::name).toList());
    }

    // ---------------------------------------------------------------- 2. Mockito

    @Test
    void listBirds_passesTheNormalisedCriteriaOnUnchanged() {
        BirdsRepository repository = mock(BirdsRepository.class);
        when(repository.findBirds(any())).thenReturn(List.of());
        BirdsSearchCriteria criteria = criteria();      // already normalised: [250, 250]

        new BirdsService(repository).listBirds(criteria);

        // The service must not re-normalise or change anything: the exact same
        // criteria object goes to the repository.
        verify(repository).findBirds(criteria);
    }

    @Test
    void listBirds_whenWoodsIsDown_theErrorPropagates() {
        BirdsRepository repository = mock(BirdsRepository.class);
        when(repository.findBirds(any()))
                .thenThrow(new BirdsRepositoryException("Could not call woods"));
        BirdsService service = new BirdsService(repository);

        BirdsRepositoryException thrown =
                assertThrows(BirdsRepositoryException.class, () -> service.listBirds(criteria()));

        assertEquals("Could not call woods", thrown.getMessage());
    }
}
