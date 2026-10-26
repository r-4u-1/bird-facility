package birds;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Switches on JAX-RS (Jakarta REST) for this WAR and sets the base path.
 *
 * Express equivalent:  app.use("/api", router)  plus  app.listen(...)
 * Except you never call it: the server finds this class at deploy time,
 * then scans the WAR for every @Path (endpoint) and @Provider (mapper,
 * filter) class and registers them.
 *
 * Resulting URLs:  http://localhost:8080/bird-facility-service/api/...
 *                                        ^ WAR name            ^ this
 */
@ApplicationPath("/api")
public class BirdsApplication extends Application {
}
