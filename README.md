# Bird facility: a Jakarta EE 10 example

Two applications:

- **bird-facility-service**: a JSON REST API for the frontend, built from the folder tree of our real project.
- **woods-system**: a stand-in for the lower business application. It only receives and answers **XML**.

```
Frontend ──JSON──▶ bird-facility-service ──XML──▶ woods-system
            GET /api/birds                 POST /legacy/birds/list
```

Every file has comments explaining what it is and why it exists.

## Folder structure

```
bird-facility/
├── pom.xml                                   parent pom: lists the modules, pins versions
│
├── bird-facility-repository/                 #2 the shared contract (plain Java, no Jakarta EE)
│   └── src/main/java/birds/
│       ├── model/
│       │   ├── AmountRange.java              THE from/to rule, in one place
│       │   ├── request/BirdsSearchCriteria.java   what to search for, clean
│       │   └── response/list/
│       │       ├── BirdsListResponse.java    {"count":..,"birds":[..]}
│       │       ├── Bird.java
│       │       ├── Feathers.java
│       │       ├── Colors.java
│       │       └── Food.java
│       └── repository/
│           ├── BirdsRepository.java          interface: "get birds from somewhere"
│           └── BirdsRepositoryException.java "the data source failed"
│
├── bird-facility-woods-repository/           #3 the woods implementation of that interface
│   └── src/main/
│       ├── java/birds/woods/
│       │   ├── model/
│       │   │   ├── request/
│       │   │   │   ├── BirdsListRequest.java     XML envelope we send
│       │   │   │   └── BirdsListBody.java        its <Body> with the search fields
│       │   │   ├── response/
│       │   │   │   ├── WoodsBirdsListResponse.java   XML envelope we receive
│       │   │   │   ├── WoodsBirdsBody.java           its <Body> with <Birds>
│       │   │   │   ├── WoodsBird.java                one <Bird>
│       │   │   │   └── WoodsBirdsFeathers.java       its <Feathers>
│       │   │   └── BirdsListParameters.java      our criteria in woods' format ("" = no filter)
│       │   └── repository/
│       │       └── BirdsRepositoryImpl.java      calls woods, converts XML <-> models
│       └── resources/META-INF/beans.xml          lets CDI find BirdsRepositoryImpl
│
├── bird-facility-service/                    #1 the REST API (WAR)
│   └── src/main/
│       ├── java/birds/
│       │   ├── BirdsApplication.java         @ApplicationPath("/api")
│       │   ├── resource/
│       │   │   ├── BirdsApi.java             GET /birds
│       │   │   └── BirdsListQuery.java       query params + validation rules
│       │   ├── core/
│       │   │   ├── BirdsValidation.java      from <= to check (only checks!)
│       │   │   └── ValidBirdsQuery.java      the annotation that triggers it
│       │   ├── service/
│       │   │   └── BirdsService.java         business rule: cheapest first
│       │   └── error/
│       │       ├── ErrorResponse.java
│       │       ├── ValidationExceptionMapper.java   -> 400
│       │       └── RepositoryExceptionMapper.java   -> 502
│       ├── resources/api-contract-birds.yaml OpenAPI contract
│       └── webapp/WEB-INF/beans.xml
│
├── bird-facility-service-integrationtest/    Cucumber tests over HTTP
│   └── src/
│       ├── main/java/birds/BirdsList.java    step definitions
│       ├── main/resources/birds/birds.feature    the scenarios
│       └── test/java/birds/RunBirdsIT.java   tells JUnit to run Cucumber
│
└── woods-system/                             the XML-only back end (WAR)
    └── src/main/
        ├── java/woods/system/
        │   ├── WoodsApplication.java         @ApplicationPath("/legacy")
        │   ├── resource/BirdsListResource.java   POST /birds/list, XML in and out
        │   ├── xml/WoodsXml.java             woods' own JAXB classes
        │   └── data/BirdCatalog.java         in-memory test data
        └── webapp/WEB-INF/beans.xml
```

A few files aren't in the original tree. Each one was needed to make the tree run:

- `AmountRange` and `BirdsSearchCriteria`: the interface needs a parameter type.
- `WoodsBird`: the XML needs a `<Bird>` element.
- `ValidBirdsQuery`: a custom validator needs an annotation.
- `BirdsListQuery`: the query DTO. In a contract-first project it would be generated from the yaml.
- The `error/` mappers.

## One request, start to finish

`GET /bird-facility-service/api/birds?facilityId=F-100&fromAmount=250`

1. **BirdsApi** receives it. JAX-RS fills a `BirdsListQuery`, and `@Valid` runs `@NotBlank`, `@Pattern` and `BirdsValidation`. If any of them fails, the method never runs and the response is a 400.
2. **BirdsListQuery.toCriteria()** calls `AmountRange.of(250, null)`, which gives the range `[250, 250]`. This is the only place that rule exists.
3. **BirdsService** calls `BirdsRepository.findBirds(...)`. It only knows the interface.
4. **CDI** injected `BirdsRepositoryImpl` from the woods JAR into the service.
5. **BirdsListParameters** turns the range into woods strings (`"250"`, `"250"`). With no range it would be `""`, `""`.
6. **BirdsRepositoryImpl** POSTs `<BirdsListRequest>` as XML to woods.
7. **woods-system** parses it, searches its catalog and answers `<BirdsListResponse>`.
8. **BirdsRepositoryImpl** converts the `Woods*` classes into `Bird`, `Feathers`, `Colors` and `Food`.
9. **BirdsService** sorts the birds (cheapest first) and wraps them in `BirdsListResponse`.
10. **JSON-B** writes it as JSON to the frontend.

The data changes shape at every border:

```
query params → BirdsListQuery → BirdsSearchCriteria → BirdsListParameters → XML → woods
woods → XML → Woods* classes → Bird/Feathers/Colors/Food → BirdsListResponse → JSON
```

### How the module boundaries are enforced

- `bird-facility-repository` has no dependencies, so it can't know about HTTP, XML or woods.
- `bird-facility-service` depends on the repository module at **compile** time and on the woods module only at **runtime**. Writing `import birds.woods...` in the service fails the build.
- `woods-system` shares no code with anything. The two sides only agree on the XML.

## Build

You need JDK 17 or newer and Maven.

```bash
mvn verify
```

This compiles everything, runs the unit tests and produces two WARs:

- `bird-facility-service/target/bird-facility-service.war`
- `woods-system/target/woods-system.war`

## Run

Use any Jakarta EE 10 server. These steps use WildFly (version 27 or newer).

**With Docker:**

```bash
mkdir -p deployments
cp bird-facility-service/target/bird-facility-service.war woods-system/target/woods-system.war deployments/
docker run --rm -p 8080:8080 \
  -v "$PWD/deployments:/opt/jboss/wildfly/standalone/deployments" \
  quay.io/wildfly/wildfly
```

**Without Docker:**

1. Download WildFly from wildfly.org and unzip it.
2. Copy both WARs into `standalone/deployments/`.
3. Run `bin/standalone.sh` (or `bin\standalone.bat` on Windows).

Both apps run on the same server, so the default woods URL works as it is. If woods runs somewhere else, start the server with `-Dwoods.url=http://host:port/woods-system/legacy/birds/list`.

## Try it

**The service (JSON):**

```bash
curl "http://localhost:8080/bird-facility-service/api/birds?facilityId=F-100"
curl "http://localhost:8080/bird-facility-service/api/birds?facilityId=F-100&fromAmount=250"
curl "http://localhost:8080/bird-facility-service/api/birds?facilityId=F-100&fromAmount=100&toAmount=300"
curl "http://localhost:8080/bird-facility-service/api/birds?facilityId=F-100&fromAmount=500&toAmount=100"   # 400
```

**Woods directly (XML):**

```bash
curl -X POST "http://localhost:8080/woods-system/legacy/birds/list" \
  -H "Content-Type: application/xml" -H "Accept: application/xml" \
  -d '<BirdsListRequest><Body><FacilityId>F-100</FacilityId><FromAmount></FromAmount><ToAmount></ToAmount></Body></BirdsListRequest>'
```

**Test data:**

| Facility | Birds and prices |
| --- | --- |
| F-100 | Sparrow 90, Blue tit 250, Robin 250, Magpie 400 |
| F-200 | Kestrel 950, Barn owl 1200 |

## Unit, Mockito and Jersey tests

All of these run with a plain `mvn verify` (or `mvn test`), with no server.

| Test | Kind | What it proves |
| --- | --- | --- |
| `AmountRangeTest` | JUnit | The from/to rule itself |
| `BirdsListQueryTest` | JUnit, parameterised | Blank (`""`, spaces) or missing amounts are normalised: one side given → used for both, none → no filter |
| `BirdsValidationTest` | JUnit | The validator only checks; blank amounts are valid and left unchanged |
| `BirdsApiTest` | JUnit + Mockito | The service receives the *normalised* criteria; a woods error isn't swallowed |
| `BirdsServiceTest` | JUnit + lambda fake + Mockito | Sorting; criteria passed on unchanged; woods error propagates |
| `ExceptionMappersTest` | JUnit + Mockito | Woods down → 502 with a safe message; validation errors → 400, sorted |
| `BirdsApiJerseyTest` | Jersey + Mockito | Real HTTP through resource, validation, service and mappers, with only the repository mocked: blank amounts normalised, woods down → 502, bad input → 400 and woods never called |
| `BirdsListParametersTest` | JUnit | Woods-format strings: `""` only when there's no range |
| `BirdsRepositoryImplJerseyTest` | Jersey | Real JAX-RS client + XML against a **fake woods server**: what we send, how answers are mapped, woods answering 503/500, woods not reachable at all |
| `BirdCatalogTest` | JUnit | Woods' search rules |
| `BirdsListResourceJerseyTest` | Jersey + Mockito | Woods' XML endpoint: blank/padded amounts, XML answer, `WoodsError` for bad input |

Notes:

- Jersey tests start a small HTTP server on **port 9998**, so that port needs to be free.
- On Java 21 Mockito prints a warning about loading its agent dynamically. It's harmless.
- On the real server CDI does the `@Inject`. In Jersey tests, Jersey's own injector (HK2) does it, configured with an `AbstractBinder` in each test.

## Integration tests

Start the server first, then run:

```bash
mvn verify -Pit -pl bird-facility-service-integrationtest
# against another environment:
mvn verify -Pit -pl bird-facility-service-integrationtest -Dbirds.baseUrl=https://test.example.com/bird-facility-service/api
```

## What was checked before delivery

The workspace this was written in couldn't reach Maven Central, so a full `mvn verify` and a real server run weren't possible there. What was checked:

- Every module, including all tests, compiled against stand-ins for the Jakarta EE, JUnit, Mockito, Jersey and Cucumber APIs. The service compiled **without** the woods module on its classpath.
- The plain unit tests passed, and so did every row of the blank-amount normalisation table and the amount regex.
- The XML round trip (criteria → woods request → woods search → woods response → models) ran in plain Java.

What still needs a real run: the Mockito and Jersey tests, the JAXB XML wire format, JSON-B output, CDI wiring and the Cucumber scenarios. Running `mvn verify` and the steps above will check all of them.
