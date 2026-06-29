Feature: See homepage

#    Rule: Authenticated user
#        Background:
#            Given I am logged in as "johanp" with password "johanp123"

        Scenario: Header
            Given I am on the homepage
            Then I can see 'Players' button