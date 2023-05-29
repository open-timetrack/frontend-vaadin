Feature: basic timeTrackViewV2 functions

  Scenario: As a user I want that the timeTrack of today is shown on the initial page to get a fast view on the today booked timings
    Given 1 timeTrack for today
    When timeTrackViewV2 is called
    Then 1 timeTrack shown

  Scenario: As a user I want the timeTrack of today be visible on the view page for yesterday
    Given 1 timeTrack for today
    When timeTrackViewV2 is called
    And view the last day
    Then 1 timeTrack shown