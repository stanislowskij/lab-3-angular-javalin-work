import { Component } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatNavList, MatListItem } from '@angular/material/list';
import { MatToolbar } from '@angular/material/toolbar';
import { MatSidenavContainer, MatSidenav, MatSidenavContent } from '@angular/material/sidenav';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: true,
    imports: [MatSidenavContainer, MatSidenav, MatToolbar, MatNavList, MatListItem, RouterLink, RouterLinkActive, MatIcon, MatSidenavContent, MatIconButton, RouterOutlet]
})
export class AppComponent {
  title = 'CSCI 3601 Lab 3';
}
