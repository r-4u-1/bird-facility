package birds.resource;

import birds.model.response.list.BirdsListResponse;
import birds.service.BirdsService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The endpoint (= controller, = an Express router).
 *
 *   GET /bird-facility-service/api/birds?facilityId=F-100&fromAmount=100
 *
 * Express equivalent:
 *   router.get("/birds", validate(schema), (req, res) =>
 *       res.json(service.listBirds(toCriteria(req.query))));
 *
 * What the server does for a request, in order:
 *   1. matches the URL to this method (@Path + @GET)
 *   2. creates a BirdsListQuery and fills it from the query string (@BeanParam)
 *   3. runs every validation rule on it (@Valid). If one fails, this method is
 *      NOT called: ValidationExceptionMapper answers 400 instead.
 *   4. calls the method with a query that is guaranteed valid
 *   5. turns the returned object into JSON (@Produces + JSON-B)
 *
 * So this class has no validation code, no null checks and no business rules.
 */
@Path("/birds")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped      // CDI: a fresh instance per HTTP request; needed for @Inject to work here
public class BirdsApi {

    @Inject         // CDI hands us the single shared BirdsService; we never write "new"
    BirdsService birdsService;

    @GET
    public BirdsListResponse listBirds(@Valid @BeanParam BirdsListQuery query) {
        return birdsService.listBirds(query.toCriteria());
    }
}
