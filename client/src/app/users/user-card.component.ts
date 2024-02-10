import { Component, input } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardActions, MatCardAvatar, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { User } from './user';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss'],
  standalone: true,
  imports: [MatCard, MatCardHeader, MatCardAvatar, MatCardTitle, MatCardSubtitle, MatCardContent, MatCardActions, MatButton, RouterLink]
})
export class UserCardComponent {
  user = input.required<User>();
  simple = input(false);
}
