package birds.resource;

import birds.error.RepositoryExceptionMapper;
import birds.error.UnreadableBodyExceptionMapper;
import birds.error.ValidationExceptionMapper;
import birds.model.AmountRange;
import birds.model.DateRange;
import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;
import birds.repository.BirdsRepository;
import birds.repository.BirdsRepositoryException;
import birds.service.BirdsService;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Jersey test: real HTTP requests against our real JAX-RS code.
 *
 * JerseyTest starts a small HTTP server (Grizzly, on port 9998 by default)
 * with exactly the classes we register in configure(), and gives us a client
 * through target(...). The closest Express idea is supertest:
 *     await request(app).get("/birds?facilityId=F-100").expect(200)
 *
 * What runs for real here:  BirdsApi, BirdsListQuery, @Valid + BirdsValidation,
 *                           BirdsService, both exception mappers, JSON-B.
 * What is mocked:           BirdsRepository (so no woods is needed).
 *
 * So these tests cover the whole service module, from the HTTP request down to
 * the repository interface and back to the HTTP response.
 */
class BirdsApiJerseyTest extends JerseyTest {

    /**
     * No "= mock(...)" here, on purpose: JerseyTest calls configure() from its
     * own constructor, BEFORE this class's field initialisers run. A field
     * initialiser would run afterwards and replace the mock that Jersey already
     * uses. So the mock is created inside configure() instead.
     */
    private BirdsRepository repository;

    /** Builds the mini application: like "const app = express(); app.use(...)". */
    @Override
    protected Application configure() {
        repository = mock(BirdsRepository.class);
        BirdsService realService = new BirdsService(repository);   // the real service, fake repository

        return new ResourceConfig()
                .register(BirdsApi.class)
                .register(ValidationExceptionMapper.class)
                .register(RepositoryExceptionMapper.class)
                .register(UnreadableBodyExceptionMapper.class)
                // On the real server CDI does @Inject. Here Jersey's own injector
                // (HK2) does it, so we tell it what to hand out for BirdsService.
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(realService).to(BirdsService.class);
                    }
                });
    }

    /*
     * JerseyTest was written for JUnit 4, so JUnit 5 doesn't call its setUp and
     * tearDown by itself. These two methods start and stop the server around
     * every test.
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static Bird bird(String name, String price) {
        return new Bird("B-" + name, name, "test species", new BigDecimal(price), null, null, null);
    }

    /** What did the service pass to the repository? (Mockito ArgumentCaptor) */
    private BirdsSearchCriteria criteriaSentToRepository() {
        ArgumentCaptor<BirdsSearchCriteria> captor = ArgumentCaptor.forClass(BirdsSearchCriteria.class);
        verify(repository).findBirds(captor.capture());
        return captor.getValue();
    }

    private static Optional<AmountRange> range(String from, String to) {
        return Optional.of(new AmountRange(new BigDecimal(from), new BigDecimal(to)));
    }

    // ------------------------------------------------------------ normalisation

    @Test
    void blankFromAmount_isNormalised_toToAmountForBothEnds() {
        // GET /birds?facilityId=F-100&fromAmount=&toAmount=300
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("fromAmount", "")
                .queryParam("toAmount", "300")
                .request().get();

        assertEquals(200, response.getStatus());
        assertEquals(range("300", "300"), criteriaSentToRepository().priceRange());
    }

    @Test
    void blankToAmount_isNormalised_toFromAmountForBothEnds() {
        // GET /birds?facilityId=F-100&fromAmount=250&toAmount=%20  (a single space)
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("fromAmount", "250")
                .queryParam("toAmount", " ")
                .request().get();

        assertEquals(200, response.getStatus());
        assertEquals(range("250", "250"), criteriaSentToRepository().priceRange());
    }

    @Test
    void missingToAmount_isNormalised_likeBlank() {
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("fromAmount", "250")
                .request().get();

        assertEquals(200, response.getStatus());
        assertEquals(range("250", "250"), criteriaSentToRepository().priceRange());
    }

    @Test
    void bothBlank_meansNoPriceFilter() {
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("fromAmount", "")
                .queryParam("toAmount", "")
                .request().get();

        assertEquals(200, response.getStatus());
        assertEquals(Optional.empty(), criteriaSentToRepository().priceRange());
    }

    // ----------------------------------------------------------------- success

    @Test
    void birdsComeBackAsJson_cheapestFirst() {
        when(repository.findBirds(any())).thenReturn(List.of(bird("Magpie", "400"), bird("Robin", "250")));

        Response response = target("birds").queryParam("facilityId", "F-100").request().get();
        String json = response.readEntity(String.class);

        assertEquals(200, response.getStatus());
        assertTrue(json.contains("\"count\":2"), json);
        assertTrue(json.indexOf("Robin") < json.indexOf("Magpie"), "Robin (250) should come first: " + json);
    }

    // ------------------------------------------------------------ woods is down

    @Test
    void woodsDown_givesA502WithASafeMessage() {
        when(repository.findBirds(any())).thenThrow(
                new BirdsRepositoryException("Could not call woods at http://woods.internal:8080"));

        Response response = target("birds").queryParam("facilityId", "F-100").request().get();
        String json = response.readEntity(String.class);

        assertEquals(502, response.getStatus());
        assertTrue(json.contains("Bird data is unavailable right now"), json);
        assertFalse(json.contains("woods.internal"), "internal details must not leak: " + json);
    }

    // -------------------------------------------------------------- validation

    @Test
    void missingFacilityId_gives400_andWoodsIsNeverCalled() {
        Response response = target("birds").queryParam("fromAmount", "100").request().get();
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("facilityId is required"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void reversedRange_gives400() {
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("fromAmount", "500")
                .queryParam("toAmount", "100")
                .request().get();

        assertEquals(400, response.getStatus());
        assertTrue(response.readEntity(String.class)
                .contains("fromAmount must be less than or equal to toAmount"));
        verifyNoInteractions(repository);
    }

    @Test
    void notANumber_gives400() {
        Response response = target("birds")
                .queryParam("facilityId", "F-100")
                .queryParam("toAmount", "abc")
                .request().get();

        assertEquals(400, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("toAmount must be a number"));
        verifyNoInteractions(repository);
    }

    // ------------------------------------------------------- POST /birds/search

    /** POSTs a JSON body, like fetch(url, {method: "POST", body: JSON.stringify(...)}). */
    private Response search(String json) {
        return target("birds/search").request().post(Entity.json(json));
    }

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void search_withDatesInsideTheLastYear_sendsTheRangeToTheRepository() {
        LocalDate from = TODAY.minusMonths(6);
        Response response = search("""
                {"facilityId": "F-100", "fromDate": "%s", "toDate": "%s"}
                """.formatted(from, TODAY));

        assertEquals(200, response.getStatus(), response.readEntity(String.class));
        BirdsSearchCriteria criteria = criteriaSentToRepository();
        assertEquals(Optional.of(new DateRange(from, TODAY)), criteria.arrivalRange());
        assertEquals(Optional.empty(), criteria.priceRange());
    }

    @Test
    void search_withAmountsAndNoDates_worksLikeGet() {
        Response response = search("""
                {"facilityId": "F-100", "fromAmount": 100, "toAmount": 300}
                """);

        assertEquals(200, response.getStatus(), response.readEntity(String.class));
        assertEquals(range("100", "300"), criteriaSentToRepository().priceRange());
        assertEquals(Optional.empty(), criteriaSentToRepository().arrivalRange());
    }

    @Test
    void search_fromDateOlderThanTwelveMonths_gives400_andWoodsIsNeverCalled() {
        Response response = search("""
                {"facilityId": "F-100", "fromDate": "%s", "toDate": "%s"}
                """.formatted(TODAY.minusMonths(12).minusDays(1), TODAY));
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("fromDate and toDate must be within the last 12 months"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void search_toDateInTheFuture_gives400() {
        Response response = search("""
                {"facilityId": "F-100", "fromDate": "%s", "toDate": "%s"}
                """.formatted(TODAY.minusMonths(1), TODAY.plusDays(1)));
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("fromDate and toDate must be within the last 12 months"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void search_reversedDates_gives400() {
        Response response = search("""
                {"facilityId": "F-100", "fromDate": "%s", "toDate": "%s"}
                """.formatted(TODAY, TODAY.minusMonths(1)));
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("fromDate must be on or before toDate"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void search_reversedAmounts_usesTheSameRuleAsGet() {
        Response response = search("""
                {"facilityId": "F-100", "fromAmount": 500, "toAmount": 100}
                """);
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("fromAmount must be less than or equal to toAmount"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void search_notADate_gives400() {
        Response response = search("""
                {"facilityId": "F-100", "fromDate": "2026-13-45"}
                """);

        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus(), json);
        assertTrue(json.contains("dates are yyyy-MM-dd"), json);
        verifyNoInteractions(repository);
    }

    @Test
    void search_missingFacilityId_gives400() {
        Response response = search("{}");
        String json = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(json.contains("facilityId is required"), json);
        verifyNoInteractions(repository);
    }
}
