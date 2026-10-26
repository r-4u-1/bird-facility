package woods.system.resource;

import woods.system.data.BirdCatalog;
import woods.system.data.BirdCatalog.StoredBird;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Jersey + Mockito test of woods' XML endpoint.
 *
 * Real:   the HTTP server, JAXB reading and writing XML, BirdsListResource.
 * Mocked: BirdCatalog, so we can check exactly what the resource asked it for.
 *
 * Woods has its own normalisation ("" or spaces mean "no bound"); these tests
 * pin that down too.
 */
class BirdsListResourceJerseyTest extends JerseyTest {

    /** Created in configure(), because JerseyTest calls it from its constructor. */
    private BirdCatalog catalog;

    @Override
    protected Application configure() {
        catalog = mock(BirdCatalog.class);
        return new ResourceConfig(BirdsListResource.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(catalog).to(BirdCatalog.class);    // what @Inject BirdCatalog receives
                    }
                });
    }

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

    /** POSTs a woods request as raw XML text, exactly like a caller would. */
    private Response post(String facilityId, String fromAmount, String toAmount) {
        return post(facilityId, fromAmount, toAmount, "", "");
    }

    private Response post(String facilityId, String fromAmount, String toAmount, String fromDate, String toDate) {
        String xml = """
                <BirdsListRequest>
                  <Body>
                    <FacilityId>%s</FacilityId>
                    <FromAmount>%s</FromAmount>
                    <ToAmount>%s</ToAmount>
                    <FromDate>%s</FromDate>
                    <ToDate>%s</ToDate>
                  </Body>
                </BirdsListRequest>
                """.formatted(facilityId, fromAmount, toAmount, fromDate, toDate);
        return target("birds/list").request(MediaType.APPLICATION_XML).post(Entity.xml(xml));
    }

    // ------------------------------------------------- woods' own normalisation

    @Test
    void emptyAmounts_meanNoBounds() {
        Response response = post("F-100", "", "");

        assertEquals(200, response.getStatus());
        verify(catalog).search("F-100", null, null, null, null);
    }

    @Test
    void spacesAroundAmounts_areTrimmed_andBlankMeansNoBound() {
        Response response = post("F-100", " 250 ", "   ");

        assertEquals(200, response.getStatus());
        verify(catalog).search("F-100", new BigDecimal("250"), null, null, null);
    }

    @Test
    void dates_arePassedToTheCatalog() {
        Response response = post("F-100", "", "", "2026-05-01", " 2026-06-30 ");

        assertEquals(200, response.getStatus());
        verify(catalog).search("F-100", null, null, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30));
    }

    @Test
    void missingDateElements_meanNoDateFilter() {
        // An older caller that doesn't know about dates yet
        String xml = "<BirdsListRequest><Body><FacilityId>F-100</FacilityId></Body></BirdsListRequest>";
        Response response = target("birds/list").request(MediaType.APPLICATION_XML).post(Entity.xml(xml));

        assertEquals(200, response.getStatus());
        verify(catalog).search("F-100", null, null, null, null);
    }

    // ------------------------------------------------- the XML answer

    @Test
    void birdsComeBackAsXml() {
        when(catalog.search(any(), any(), any(), any(), any())).thenReturn(List.of(new StoredBird(
                "B-1", "Robin", "Erithacus rubecula", new BigDecimal("250.00"),
                "Insects", 12, "Contour", 65, "Brown", null, LocalDate.of(2026, 5, 1))));

        Response response = post("F-100", "250", "250");
        String xml = response.readEntity(String.class);

        assertEquals(200, response.getStatus());
        assertTrue(xml.contains("<Name>Robin</Name>"), xml);
        assertTrue(xml.contains("<Price>250.00</Price>"), xml);
        assertTrue(xml.contains("<ArrivalDate>2026-05-01</ArrivalDate>"), xml);
        // A missing secondary colour is sent as an empty element, woods' convention.
        assertTrue(xml.contains("<SecondaryColor></SecondaryColor>") || xml.contains("<SecondaryColor/>"), xml);
    }

    // ------------------------------------------------- woods' own errors

    @Test
    void missingFacility_gives400WithAWoodsError_andTheCatalogIsNotCalled() {
        Response response = post("", "", "");
        String xml = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(xml.contains("<Code>MISSING_FACILITY</Code>"), xml);
        verifyNoInteractions(catalog);
    }

    @Test
    void notANumber_gives400WithAWoodsError() {
        Response response = post("F-100", "abc", "");
        String xml = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(xml.contains("<Code>BAD_AMOUNT</Code>"), xml);
        verifyNoInteractions(catalog);
    }

    @Test
    void notADate_gives400WithAWoodsError() {
        Response response = post("F-100", "", "", "2026-13-45", "");
        String xml = response.readEntity(String.class);

        assertEquals(400, response.getStatus());
        assertTrue(xml.contains("<Code>BAD_DATE</Code>"), xml);
        verifyNoInteractions(catalog);
    }
}
