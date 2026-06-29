import {Given} from "@badeball/cypress-cucumber-preprocessor";

Given(
    "I am on the homepage",
    (page: string) => {
      cy.visit('http://localhost:8000');
    }
);