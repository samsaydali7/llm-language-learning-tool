import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  readonly navItems: NavItem[] = [
    { path: '/', label: 'Dashboard', icon: 'space_dashboard' },
    { path: '/languages', label: 'Languages', icon: 'translate' },
    { path: '/books', label: 'Books', icon: 'menu_book' },
    { path: '/topics', label: 'Topics', icon: 'sell' },
    { path: '/exercises', label: 'Exercises', icon: 'edit_note' },
    { path: '/flashcards', label: 'Flashcards', icon: 'style' },
    { path: '/listening', label: 'Listening', icon: 'headphones' },
    { path: '/grammar', label: 'Grammar review', icon: 'auto_stories' },
    { path: '/review', label: 'Review mistakes', icon: 'history' },
  ];
}
