Feature: Test Resource API
  Users should be able to submit POST, GET, DELETE requests to the Resource Service

  Scenario: Data upload to Resource Service
    Given File test.mp3 with mp3
    When Users upload file to Resource Service
    Then Resource Service should handle it and return success status

  Scenario: Data getting from Resource Service
    Given The following resources to get
      | filename  |
      | temp1.mp3 |
      | temp2.mp3 |
    When Users request data from Resource Service
    Then Resource Service should return requested data

  Scenario: Data deleting from Resource Service
    Given The following resources to delete
      | filename  |
      | temp1.mp3 |
      | temp2.mp3 |
    When Users wants to delete specified data in Resource Service
    Then Resource Service should delete it