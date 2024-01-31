Feature: basic timeTrackView functions

  Scenario: As a user I want that the timeTrack of today is shown on the initial page to get a fast view on the today booked timings
    Given 1 timeTrack for today
    When timeTrackViewV1 is called
    Then 1 timeTrack shown

  Scenario: As a user I want the timeTrack of today no be visible on the view page for yesterday
    Given 1 timeTrack for today
    When timeTrackViewV1 is called
    And view the last day
    Then 0 timeTrack shown

  Scenario: As a user I want the timeTrack of today be visible after switching to yesterday and back
    Given 1 timeTrack for today
    When timeTrackViewV1 is called
    And view the last day
    And view the next day
    Then 1 timeTrack shown