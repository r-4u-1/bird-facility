# birds.feature
#
# Test scenarios in Gherkin: plain-language Given / When / Then steps.
# Each step line is matched by its text to a Java method in BirdsList.java
# (the "step definitions"). Text in quotes and numbers become method arguments.
#
# The expected numbers come from the woods system's test data:
#   F-100: Sparrow 90, Blue tit 250, Robin 250, Magpie 400
#   F-200: Kestrel 950, Barn owl 1200

Feature: List the birds in a facility

  Scenario: All birds in a facility, cheapest first
    When I request birds with "facilityId=F-100"
    Then the response status is 200
    And the response contains 4 birds
    And the first bird is "Sparrow"

  Scenario: Only fromAmount means exactly that price
    # AmountRange.of copies 250 into toAmount, so the range is [250, 250]
    When I request birds with "facilityId=F-100&fromAmount=250"
    Then the response status is 200
    And the response contains 2 birds

  Scenario: A blank fromAmount counts as not sent
    # fromAmount= is normalised to "not sent", then 250 is used for both ends
    When I request birds with "facilityId=F-100&fromAmount=&toAmount=250"
    Then the response status is 200
    And the response contains 2 birds

  Scenario: Both amounts blank means no price filter
    When I request birds with "facilityId=F-100&fromAmount=&toAmount="
    Then the response status is 200
    And the response contains 4 birds

  Scenario: A price range
    When I request birds with "facilityId=F-100&fromAmount=100&toAmount=300"
    Then the response status is 200
    And the response contains 2 birds

  Scenario: An unknown facility has no birds
    When I request birds with "facilityId=F-999"
    Then the response status is 200
    And the response contains 0 birds

  Scenario: facilityId is required
    When I request birds with "fromAmount=100"
    Then the response status is 400
    And the error mentions "facilityId is required"

  Scenario: A reversed range is rejected before woods is called
    When I request birds with "facilityId=F-100&fromAmount=500&toAmount=100"
    Then the response status is 400
    And the error mentions "fromAmount must be less than or equal to toAmount"

  Scenario: A badly formatted amount is rejected
    When I request birds with "facilityId=F-100&toAmount=abc"
    Then the response status is 400
    And the error mentions "toAmount must be a number"
