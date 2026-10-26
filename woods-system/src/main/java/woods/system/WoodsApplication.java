package woods.system;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Switches on JAX-RS for the woods WAR.
 *
 * URLs:  http://localhost:8080/woods-system/legacy/...
 *                              ^ WAR name   ^ this
 *
 * "/legacy" is just a name that hints at what this plays: an older back-end.
 */
@ApplicationPath("/legacy")
public class WoodsApplication extends Application {
}
