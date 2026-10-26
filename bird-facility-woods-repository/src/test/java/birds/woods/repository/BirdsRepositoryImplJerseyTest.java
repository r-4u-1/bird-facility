package birds.woods.repository;

import birds.model.AmountRange;
import birds.model.DateRange;
import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;
import birds.repository.BirdsRepositoryException;
import birds.woods.model.request.BirdsListBody;
import birds.woods.model.request.BirdsListRequest;
import birds.woods.model.response.WoodsBird;
import birds.woods.model.response.WoodsBirdsBody;
import birds.woods.model.response.WoodsBirdsFeathers;
import birds.woods.model.response.WoodsBirdsListResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Jersey test with a FAKE woods server.
 *
 * JerseyTest starts a real HTTP server (port 9998) that serves
 * FakeWoodsResource below. BirdsRepositoryImpl is then pointed at it with the
 * same -Dwoods.url property it uses in production, so its real code runs:
 * the JAX-RS client, the JAXB XML in both directions, the status handling and
 * the mapping to our models.
 *
 * It answers three questions:
 *   1. What exactly do we SEND to woods, for blank or one-sided amounts?
 *   2. Do we turn woods' XML answer into the right models?
 *   3. What happens when woods answers an error, or isn't there at all?
 *
 * This test lives in the same package as BirdsRepositoryImpl, so it may call
 * its package-private init() and close(), standing in for CDI.
 */
class BirdsRepositoryImplJerseyTest extends JerseyTest {

    /**
     * The fake woods endpoint, served by JerseyTest. It records the last
     * request and answers whatever the test asks for. The static fields are
     * shared state, so they are reset before every test.
     */
    @Path("legacy/birds/list")
    public static class FakeWoodsResource {

        static volatile BirdsListBody lastRequest;
        static volatile int statusToAnswer = 200;

        @POST
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public Response list(BirdsListRequest request) {
            lastRequest = request.body;
            if (statusToAnswer != 200) {
                return Response.status(statusToAnswer).build();
            }
            return Response.ok(robinAsWoodsSendsIt()).build();
        }
    }

    private BirdsRepositoryImpl repository;

    @Override
    protected Application configure() {
        return new ResourceConfig(FakeWoodsResource.class);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();                                   // starts the fake woods server
        FakeWoodsResource.lastRequest = null;
        FakeWoodsResource.statusToAnswer = 200;
        repository = repositoryCalling(target("legacy/birds/list").getUri().toString());
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        repository.close();
        System.clearProperty("woods.url");
        super.tearDown();                                // stops the fake woods server
    }

    /** Creates the repository the way CDI would: constructor, then @PostConstruct. */
    private static BirdsRepositoryImpl repositoryCalling(String woodsUrl) {
        System.setProperty("woods.url", woodsUrl);
        BirdsRepositoryImpl repository = new BirdsRepositoryImpl();
        repository.init();
        return repository;
    }

    private static BirdsSearchCriteria criteria(String from, String to) {
        return new BirdsSearchCriteria("F-100", AmountRange.of(
                from == null ? null : new BigDecimal(from),
                to == null ? null : new BigDecimal(to)));
    }

    // -------------------------------------------- 1. what we send to woods

    @Test
    void onlyToAmount_isSentToWoodsAsBothEnds() {
        repository.findBirds(criteria(null, "300"));

        assertEquals("F-100", FakeWoodsResource.lastRequest.facilityId);
        assertEquals("300", FakeWoodsResource.lastRequest.fromAmount);
        assertEquals("300", FakeWoodsResource.lastRequest.toAmount);
    }

    @Test
    void onlyFromAmount_isSentToWoodsAsBothEnds() {
        repository.findBirds(criteria("250.50", null));

        assertEquals("250.50", FakeWoodsResource.lastRequest.fromAmount);
        assertEquals("250.50", FakeWoodsResource.lastRequest.toAmount);
    }

    @Test
    void noAmounts_areSentAsEmptyElements_becauseThatIsWoodsFormat() {
        repository.findBirds(criteria(null, null));

        // <FromAmount></FromAmount> arrives as "" on the woods side.
        assertEquals("", FakeWoodsResource.lastRequest.fromAmount);
        assertEquals("", FakeWoodsResource.lastRequest.toAmount);
        assertEquals("", FakeWoodsResource.lastRequest.fromDate);
        assertEquals("", FakeWoodsResource.lastRequest.toDate);
    }

    @Test
    void arrivalRange_isSentToWoodsAsIsoDates() {
        repository.findBirds(new BirdsSearchCriteria("F-100", Optional.empty(),
                DateRange.of(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30))));

        assertEquals("2026-05-01", FakeWoodsResource.lastRequest.fromDate);
        assertEquals("2026-06-30", FakeWoodsResource.lastRequest.toDate);
    }

    // -------------------------------------------- 2. woods' answer -> our models

    @Test
    void woodsXml_isMappedToOurModels() {
        List<Bird> birds = repository.findBirds(criteria(null, null));

        assertEquals(1, birds.size());
        Bird robin = birds.get(0);
        assertEquals("Robin", robin.name());
        assertEquals(new BigDecimal("250.00"), robin.price());
        assertEquals(65, robin.feathers().lengthMm());
        assertEquals("Brown", robin.feathers().colors().primary());
        assertNull(robin.feathers().colors().secondary(), "woods' empty <SecondaryColor> becomes null");
        assertEquals(12, robin.food().gramsPerDay());
        assertEquals(LocalDate.of(2026, 5, 1), robin.arrivedOn());
    }

    // -------------------------------------------- 3. woods errors

    @Test
    void woodsAnswers503_becomesBirdsRepositoryException() {
        FakeWoodsResource.statusToAnswer = 503;

        BirdsRepositoryException thrown =
                assertThrows(BirdsRepositoryException.class, () -> repository.findBirds(criteria(null, null)));

        assertTrue(thrown.getMessage().contains("503"), thrown.getMessage());
    }

    @Test
    void woodsAnswers500_becomesBirdsRepositoryException() {
        FakeWoodsResource.statusToAnswer = 500;

        assertThrows(BirdsRepositoryException.class, () -> repository.findBirds(criteria(null, null)));
    }

    @Test
    void woodsIsDown_becomesBirdsRepositoryException() throws IOException {
        // A port where nothing listens: the connection is refused, like a woods
        // server that's switched off.
        BirdsRepositoryImpl pointingNowhere = repositoryCalling("http://localhost:" + freePort() + "/woods");
        try {
            BirdsRepositoryException thrown = assertThrows(BirdsRepositoryException.class,
                    () -> pointingNowhere.findBirds(criteria(null, null)));

            assertTrue(thrown.getMessage().startsWith("Could not call woods"), thrown.getMessage());
            // The original low-level error is kept as the cause, for the logs.
            // (Jersey wraps it in a WebApplicationException, other clients in a ProcessingException.)
            assertNotNull(thrown.getCause());
        } finally {
            pointingNowhere.close();
        }
    }

    // -------------------------------------------- helpers

    /** Asks the OS for a free port, then releases it, so nothing is listening there. */
    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /** The XML answer the fake woods sends: one robin, with an empty secondary colour. */
    private static WoodsBirdsListResponse robinAsWoodsSendsIt() {
        WoodsBirdsFeathers feathers = new WoodsBirdsFeathers();
        feathers.type = "Contour";
        feathers.lengthMm = "65";
        feathers.primaryColor = "Brown";
        feathers.secondaryColor = "";

        WoodsBird robin = new WoodsBird();
        robin.id = "B-1";
        robin.name = "Robin";
        robin.species = "Erithacus rubecula";
        robin.price = "250.00";
        robin.foodType = "Insects";
        robin.foodGramsPerDay = "12";
        robin.feathers = feathers;
        robin.arrivalDate = "2026-05-01";

        WoodsBirdsBody body = new WoodsBirdsBody();
        body.birds = List.of(robin);

        WoodsBirdsListResponse response = new WoodsBirdsListResponse();
        response.body = body;
        return response;
    }
}
