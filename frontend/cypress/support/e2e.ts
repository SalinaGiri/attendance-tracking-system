// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'
import {resetDatabaseUrl} from "./urls";

Cypress.Commands.add("resetDatabase", () => {
    return cy.request<String>({
        method: "GET",
        url: resetDatabaseUrl,
    }).then((response) => {
        expect(response.status).to.eq(200);
        return response.body;
    });
});

Cypress.on('uncaught:exception', (err, runnable) => {
    // returning false here prevents Cypress from
    // failing the test
    return false
})

declare global {
    namespace Cypress {
        interface Chainable {
            // login(username: string, password: string): Chainable<void>;
            // logout(): Chainable<void>;
            resetDatabase(): Chainable<String>;
        }
    }
}