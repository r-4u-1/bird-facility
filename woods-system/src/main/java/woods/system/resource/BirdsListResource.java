package woods.system.resource;

import woods.system.data.BirdCatalog;
import woods.system.data.BirdCatalog.StoredBird;
import woods.system.xml.WoodsXml;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Woods' only endpoint: an XML-in, XML-out search.
 *
 *   POST /woods-system/legacy/birds/list
 *   Content-Type: application/xml
 *
 *   <BirdsListRequest><Body>...</Body></BirdsListRequest>
 *
 * @Consumes(XML): JAX-RS only accepts XML bodies here and uses JAXB to turn
 *                 the body into a WoodsXml.BirdsListRequest object for us.
 * @Produces(XML): whatever we return is written back as XML by JAXB.
 *
 * Express equivalent (with an XML body parser):
 *   router.post("/birds/list", (req, res) => res.type("xml").send(toXml(search(req.body))));
 */
@Path("/birds/list")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
@RequestScoped
public class BirdsListResource {

    private static final Logger LOG = Logger.getLogger(BirdsListResource.class.getName());

    @Inject
    BirdCatalog catalog;

    /**
     * Returns a Response (instead of a plain object) because we need to choose
     * the status code ourselves: 200 with birds, or 400 with a WoodsError.
     */
    @POST
    public Response listBirds(WoodsXml.BirdsListRequest request) {
        // An XML body with no annotation on the parameter = the request body.
        if (request == null || request.body == null || isBlank(request.body.facilityId)) {
            return badRequest("MISSING_FACILITY", "FacilityId is required");
        }

        WoodsXml.RequestBody body = request.body;
        LOG.info("Woods received: facility=" + body.facilityId
                + " from='" + body.fromAmount + "' to='" + body.toAmount + "'");

        BigDecimal min;
        BigDecimal max;
        try {
            min = parseAmount(body.fromAmount);
            max = parseAmount(body.toAmount);
        } catch (NumberFormatException e) {
            return badRequest("BAD_AMOUNT", "FromAmount and ToAmount must be numbers or empty");
        }

        List<WoodsXml.BirdElement> birds = catalog.search(body.facilityId, min, max)
                .stream()
                .map(BirdsListResource::toXml)
                .toList();

        WoodsXml.ResponseBody responseBody = new WoodsXml.ResponseBody();
        responseBody.birds = birds;
        WoodsXml.BirdsListResponse response = new WoodsXml.BirdsListResponse();
        response.body = responseBody;

        return Response.ok(response).build();      // 200 + XML via JAXB
    }

    /** Woods' quirk lives here: "" (or a missing element) means "no filter". */
    private static BigDecimal parseAmount(String text) {
        return isBlank(text) ? null : new BigDecimal(text.trim());
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private static Response badRequest(String code, String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new WoodsXml.WoodsError(code, message))
                .build();
    }

    /** Woods' internal record -> woods' XML element. Everything becomes text. */
    private static WoodsXml.BirdElement toXml(StoredBird bird) {
        WoodsXml.FeathersElement feathers = new WoodsXml.FeathersElement();
        feathers.type = bird.featherType();
        feathers.lengthMm = String.valueOf(bird.featherLengthMm());
        feathers.primaryColor = bird.primaryColor();
        feathers.secondaryColor = bird.secondaryColor() == null ? "" : bird.secondaryColor();

        WoodsXml.BirdElement element = new WoodsXml.BirdElement();
        element.id = bird.id();
        element.name = bird.name();
        element.species = bird.species();
        element.price = bird.price().toPlainString();
        element.foodType = bird.foodType();
        element.foodGramsPerDay = String.valueOf(bird.foodGramsPerDay());
        element.feathers = feathers;
        return element;
    }
}
