import { UserListPage } from '../support/user-list.po';

const page = new UserListPage();

describe('User list', () => {

  beforeEach(() => {
    page.navigateTo();
  });

  it('Should have the correct page title', () => {
    page.getPageTitle().should('eq', 'Users');
  });

  it('Should have the correct title', () => {
    page.getUserTitle().should('have.text', 'Users');
  });

  it('Should type something in the name filter and check that it returned correct elements', () => {
    // Filter for user 'Lynn Ferguson'
    cy.get('[data-test=userNameInput]').type('Lynn Ferguson');

    // All of the user cards should have the name we are filtering by
    page.getUserCards().each($card => {
      cy.wrap($card).find('.user-card-name').should('have.text', 'Lynn Ferguson');
    });

    // (We check this two ways to show multiple ways to check this)
    page.getUserCards().find('.user-card-name').each($name =>
      expect($name.text()).to.equal('Lynn Ferguson')
    );
  });

  it('Should type something in the company filter and check that it returned correct elements', () => {
    // Filter for company 'OHMNET'
    cy.get('[data-test=userCompanyInput]').type('OHMNET');

    page.getUserCards().should('have.lengthOf.above', 0);

    // All of the user cards should have the company we are filtering by
    page.getUserCards().find('.user-card-company').each($card => {
      cy.wrap($card).should('have.text', 'OHMNET');
    });
  });

  it('Should type something partial in the company filter and check that it returned correct elements', () => {
    // Filter for companies that contain 'ti'
    cy.get('[data-test=userCompanyInput]').type('ti');

    page.getUserCards().should('have.lengthOf', 2);

    // Each user card's company name should include the text we are filtering by
    page.getUserCards().each(e => {
      cy.wrap(e).find('.user-card-company').should('include.text', 'TI');
    });
  });

  it('Should type something in the age filter and check that it returned correct elements', () => {
    // Filter for users of age '27'
    cy.get('[data-test=userAgeInput]').type('27');

    page.getUserCards().should('have.lengthOf', 3);

    // Go through each of the cards that are being shown and get the names
    page.getUserCards().find('.user-card-name')
      // We should see these users whose age is 27
      .should('contain.text', 'Stokes Clayton')
      .should('contain.text', 'Bolton Monroe')
      .should('contain.text', 'Merrill Parker')
      // We shouldn't see these users
      .should('not.contain.text', 'Connie Stewart')
      .should('not.contain.text', 'Lynn Ferguson');
  });

  it('Should change the view', () => {
    // Choose the view type "List"
    page.changeView('list');

    // We should not see any cards
    // There should be list items
    page.getUserCards().should('not.exist');
    page.getUserListItems().should('exist');

    // Choose the view type "Card"
    page.changeView('card');

    // There should be cards
    // We should not see any list items
    page.getUserCards().should('exist');
    page.getUserListItems().should('not.exist');
  });

  it('Should select a role, switch the view, and check that it returned correct elements', () => {
    // Filter for role 'viewer');
    page.selectRole('viewer');

    // Choose the view type "List"
    page.changeView('list');

    // Some of the users should be listed
    page.getUserListItems().should('have.lengthOf.above', 0);

    // All of the user list items that show should have the role we are looking for
    page.getUserListItems().each($user => {
      cy.wrap($user).find('.user-list-role').should('contain', 'viewer');
    });
  });

  it('Should click view profile on a user and go to the right URL', () => {
    page.getUserCards().first().then((card) => {
      const firstUserName = card.find('.user-card-name').text();
      const firstUserCompany = card.find('.user-card-company').text();

      // When the view profile button on the first user card is clicked, the URL should have a valid mongo ID
      page.clickViewProfile(page.getUserCards().first());

      // The URL should contain '/users/' (note the ending slash) and '/users/' should be followed by a mongo ID
      cy.url()
        .should('contain', '/users/')
        .should('match', /.*\/users\/[0-9a-fA-F]{24}$/);

      // On this profile page we were sent to, the name and company should be correct
      cy.get('.user-card-name').first().should('have.text', firstUserName);
      cy.get('.user-card-company').first().should('have.text', firstUserCompany);
    });
   });

});
