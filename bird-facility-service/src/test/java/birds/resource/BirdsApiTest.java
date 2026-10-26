package birds.resource;

import birds.model.AmountRange;
import birds.model.request.BirdsSearchCriteria;
import birds.repository.BirdsRepositoryException;
import birds.service.BirdsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit + Mockito: the resource class on its own, with the service mocked.
 * No HTTP here; BirdsApiJerseyTest covers the HTTP side.
 *
 * Mockito in TypeScript terms:
 *   @Mock         -> const birdsService = jest.mocked(new BirdsService())
 *   @InjectMocks  -> creates a BirdsApi and puts the mocks into its fields
 *   when(...).thenThrow(...)   -> mockFn.mockImplementation(() => { throw ... })
 *   verify(mock).method(...)   -> expect(mockFn).toHaveBeenCalledWith(...)
 *   ArgumentCaptor             -> mockFn.mock.calls[0][0]: grab what was passed
 *
 * MockitoExtension also fails a test if it stubs something that is never used,
 * which keeps tests honest.
 */
@ExtendWith(MockitoExtension.class)
class BirdsApiTest {

    @Mock
    BirdsService birdsService;

    @InjectMocks
    BirdsApi birdsApi;

    @Captor
    ArgumentCaptor<BirdsSearchCriteria> criteria;

    private static BirdsListQuery query(String from, String to) {
        BirdsListQuery query = new BirdsListQuery();
        query.setFacilityId("F-100");
        query.setFromAmount(from);
        query.setToAmount(to);
        return query;
    }

    @Test
    void blankFromAmount_reachesTheServiceNormalised() {
        birdsApi.listBirds(query("", "300"));

        verify(birdsService).listBirds(criteria.capture());
        assertEquals("F-100", criteria.getValue().facilityId());
        assertEquals(Optional.of(new AmountRange(new BigDecimal("300"), new BigDecimal("300"))),
                criteria.getValue().priceRange());
    }

    @Test
    void blankToAmount_reachesTheServiceNormalised() {
        birdsApi.listBirds(query("250", " "));

        verify(birdsService).listBirds(criteria.capture());
        assertEquals(Optional.of(new AmountRange(new BigDecimal("250"), new BigDecimal("250"))),
                criteria.getValue().priceRange());
    }

    @Test
    void bothBlank_reachesTheServiceAsNoFilter() {
        birdsApi.listBirds(query("", ""));

        verify(birdsService).listBirds(criteria.capture());
        assertEquals(Optional.empty(), criteria.getValue().priceRange());
    }

    /**
     * When woods is down, the resource does NOT catch the error: it lets it
     * travel up to RepositoryExceptionMapper, which turns it into a 502.
     */
    @Test
    void woodsDown_errorIsNotSwallowedByTheResource() {
        BirdsRepositoryException woodsDown = new BirdsRepositoryException("Could not call woods");
        when(birdsService.listBirds(any())).thenThrow(woodsDown);

        BirdsRepositoryException thrown =
                assertThrows(BirdsRepositoryException.class, () -> birdsApi.listBirds(query(null, null)));

        assertSame(woodsDown, thrown);
    }
}
