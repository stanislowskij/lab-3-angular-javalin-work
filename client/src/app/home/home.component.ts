import {Component} from '@angular/core';
import { MatCard } from '@angular/material/card';

@Component({
    selector: 'app-home-component',
    templateUrl: 'home.component.html',
    styleUrls: ['./home.component.scss'],
    providers: [],
    standalone: true,
    imports: [MatCard]
})
export class HomeComponent {}
