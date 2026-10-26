package birds;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Step definitions for birds.feature.
 *
 * Each annotated method matches one kind of step line. The {string} and {int}
 * placeholders are Cucumber expressions: they capture the quoted text or the
 * number from the line and pass it in as an argument.
 *
 * Cucumber creates a NEW instance of this class for every scenario, so the
 * "response" field never leaks from one scenario into the next.
 *
 * Uses java.net.http.HttpClient from the standard library (Java's fetch),
 * so this module needs no Jakarta EE and no service code at all.
 */
public class BirdsList {

    /** Where the deployed service lives. Override with -Dbirds.baseUrl=... */
    private static final String BASE_URL =
            System.getProperty("birds.baseUrl", "http://localhost:8080/bird-facility-service/api");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private HttpResponse<String> response;

    @When("I request birds with {string}")
    public void iRequestBirdsWith(String query) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/birds?" + query))
                .header("Accept", "application/json")
                .GET()
                .build();
        // send() is blocking; it's the synchronous version of await fetch(...)
        response = http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * A step followed by a """ block (a "doc string"): Cucumber passes the
     * block's text as the last argument.
     */
    @When("I search birds with:")
    public void iSearchBirdsWith(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/birds/search"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(withDates(json)))
                .build();
        response = http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Replaces {today}, {tomorrow} and {N months ago} with real yyyy-MM-dd dates. */
    private static String withDates(String text) {
        LocalDate today = LocalDate.now();
        Matcher monthsAgo = Pattern.compile("\\{(\\d+) months ago}").matcher(text);
        String replaced = monthsAgo.replaceAll(m -> today.minusMonths(Long.parseLong(m.group(1))).toString());
        return replaced
                .replace("{today}", today.toString())
                .replace("{tomorrow}", today.plusDays(1).toString());
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expected) {
        assertEquals(expected, response.statusCode(), "Body was: " + response.body());
    }

    @Then("the response contains {int} birds")
    public void theResponseContainsBirds(int expected) {
        assertEquals(expected, readInt("count"), "Body was: " + response.body());
    }

    @Then("the first bird is {string}")
    public void theFirstBirdIs(String expectedName) {
        // Simple text matching keeps the module free of a JSON library. The
        // first "name" in the body belongs to the first bird in the list.
        Matcher matcher = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"").matcher(response.body());
        if (!matcher.find()) {
            fail("No bird name in: " + response.body());
        }
        assertEquals(expectedName, matcher.group(1));
    }

    @Then("the error mentions {string}")
    public void theErrorMentions(String text) {
        assertTrue(response.body().contains(text),
                "Expected the error to mention '" + text + "' but was: " + response.body());
    }

    /** Reads a top-level number like "count":4 from the JSON body. */
    private int readInt(String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)").matcher(response.body());
        if (!matcher.find()) {
            fail("No '" + field + "' in: " + response.body());
        }
        return Integer.parseInt(matcher.group(1));
    }
}
