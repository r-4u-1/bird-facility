package birds.woods.repository;

import birds.model.request.BirdsSearchCriteria;
import birds.model.response.list.Bird;
import birds.model.response.list.Colors;
import birds.model.response.list.Feathers;
import birds.model.response.list.Food;
import birds.repository.BirdsRepository;
import birds.repository.BirdsRepositoryException;
import birds.woods.model.BirdsListParameters;
import birds.woods.model.request.BirdsListRequest;
import birds.woods.model.response.WoodsBird;
import birds.woods.model.response.WoodsBirdsFeathers;
import birds.woods.model.response.WoodsBirdsListResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The woods implementation of BirdsRepository: the only class that talks to
 * the woods system.
 *
 * What one call does, in order:
 *   1. our criteria  -> BirdsListParameters (woods' strings, "" for no filter)
 *   2. parameters    -> BirdsListRequest    (JAXB object)
 *   3. POST it as XML to woods, read the XML answer into WoodsBirdsListResponse
 *   4. WoodsBird...  -> Bird, Feathers, Colors, Food (our models)
 *
 * Models go in and models come out. Nothing above this class ever sees XML.
 *
 * @ApplicationScoped: CDI creates ONE instance for the whole app. Because this
 * class implements BirdsRepository, CDI injects it wherever someone asks for
 * a BirdsRepository (that's BirdsService).
 */
@ApplicationScoped
public class BirdsRepositoryImpl implements BirdsRepository {

    private static final Logger LOG = Logger.getLogger(BirdsRepositoryImpl.class.getName());

    /** Where woods listens. Override per environment with -Dwoods.url=... on the server. */
    private static final String DEFAULT_WOODS_URL = "http://localhost:8080/woods-system/legacy/birds/list";

    private String woodsUrl;
    private Client httpClient;

    /**
     * Runs once, after CDI created the object. Setup goes here rather than in
     * a constructor, because CDI may create extra proxy instances of the class.
     */
    @PostConstruct
    void init() {
        woodsUrl = System.getProperty("woods.url", DEFAULT_WOODS_URL);
        // The JAX-RS client is thread-safe and expensive to create: make one, reuse it.
        httpClient = ClientBuilder.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        LOG.info("Woods repository will call " + woodsUrl);
    }

    /** Runs when the app is undeployed or the server stops: free the connections. */
    @PreDestroy
    void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    @Override   // "this implements a method from the interface"; the compiler checks the signature
    public List<Bird> findBirds(BirdsSearchCriteria criteria) {
        BirdsListRequest request = BirdsListParameters.from(criteria).toRequest();   // steps 1 + 2
        WoodsBirdsListResponse response = post(request);                            // step 3
        return toModel(response);                                                   // step 4
    }

    // ---------------------------------------------------------------- HTTP

    /**
     * POSTs the request as XML and reads the XML answer.
     *
     * MediaType.APPLICATION_XML tells the JAX-RS client to use JAXB: it turns
     * BirdsListRequest into XML text for us, and the answer back into
     * WoodsBirdsListResponse. No string building, no manual parsing.
     *
     * try (...) = try-with-resources: the Response is closed automatically.
     */
    private WoodsBirdsListResponse post(BirdsListRequest request) {
        try (Response response = httpClient.target(woodsUrl)
                .request(MediaType.APPLICATION_XML)          // Accept: application/xml
                .post(Entity.xml(request))) {               // Content-Type: application/xml

            if (response.getStatus() != 200) {
                throw new BirdsRepositoryException("Woods answered HTTP " + response.getStatus());
            }
            return response.readEntity(WoodsBirdsListResponse.class);

        } catch (ProcessingException e) {
            // Timeout, connection refused, unreadable XML...
            throw new BirdsRepositoryException("Could not call woods at " + woodsUrl, e);
        }
    }

    // ---------------------------------------------------------- XML -> model

    /** The whole response. Defensive about missing parts, because it's an external system. */
    private static List<Bird> toModel(WoodsBirdsListResponse response) {
        if (response == null || response.body == null || response.body.birds == null) {
            return List.of();
        }
        return response.body.birds.stream()
                .map(BirdsRepositoryImpl::toBird)       // method reference: same as b -> toBird(b)
                .toList();
    }

    /** One woods <Bird> -> our Bird. Text becomes real types; flat food fields become a Food record. */
    private static Bird toBird(WoodsBird woods) {
        return new Bird(
                woods.id,
                woods.name,
                woods.species,
                new BigDecimal(woods.price),
                toFeathers(woods.feathers),
                new Food(woods.foodType, Integer.parseInt(woods.foodGramsPerDay)));
    }

    /** Woods' flat feathers -> our Feathers with a nested Colors record. */
    private static Feathers toFeathers(WoodsBirdsFeathers woods) {
        if (woods == null) {
            return null;
        }
        String secondary = (woods.secondaryColor == null || woods.secondaryColor.isBlank())
                ? null                      // woods sends "" for "none"; our model says null
                : woods.secondaryColor;
        return new Feathers(
                woods.type,
                Integer.parseInt(woods.lengthMm),
                new Colors(woods.primaryColor, secondary));
    }
}
