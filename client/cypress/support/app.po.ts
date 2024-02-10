export class AppPage {
  navigateTo() {
    return cy.visit('/');
  }

  getPageTitle() {
    return cy.title();
  }

  getAppTitle() {
    return cy.get('.app-title');
  }

  getSidenavButton() {
    return cy.get('.sidenav-button');
  }

  getSidenav() {
    return cy.get('.sidenav');
  }

  getNavLink(navOption: string) {
    return cy.contains('[routerlink] > .mdc-list-item__content', navOption);
  }
}
