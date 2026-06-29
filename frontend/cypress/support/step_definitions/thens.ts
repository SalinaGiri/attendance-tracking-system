import {Then} from "@badeball/cypress-cucumber-preprocessor";


Then(
    "I can see {string} button",
    (btnName: string) => {
        cy.get('nav a:nth-child(2)').should('contain.text', btnName);
    }
)