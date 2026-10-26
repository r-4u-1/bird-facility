package birds;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * The entry point that makes JUnit run the Cucumber features. It has no code
 * of its own; the annotations are the configuration:
 *
 *   @Suite                         a group of tests that JUnit runs together
 *   @IncludeEngines("cucumber")    run them with the Cucumber engine
 *   @SelectClasspathResource       the features are in the "birds" folder
 *                                  (src/main/resources/birds/birds.feature)
 *   GLUE = "birds"                 the step definitions are in package "birds"
 *   PLUGIN = "pretty"              print each step in the console as it runs
 *
 * The name ends in IT, so the failsafe plugin (not surefire) runs it, and only
 * when the "it" profile is switched on.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("birds")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "birds")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class RunBirdsIT {
}
