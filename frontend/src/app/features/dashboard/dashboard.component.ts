import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { LanguageService } from '../../core/services/language.service';
import { BookService } from '../../core/services/book.service';

interface QuickLink {
  path: string;
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  languageCount = 0;
  bookCount = 0;

  readonly quickLinks: QuickLink[] = [
    { path: '/books', icon: 'menu_book', title: 'Browse books', description: 'Upload a PDF and audio, and explore its structure and knowledge base.' },
    { path: '/exercises', icon: 'edit_note', title: 'Practice exercises', description: 'Generate exercises from a topic, chapter, or the whole book.' },
    { path: '/flashcards', icon: 'style', title: 'Study flashcards', description: 'Instant flashcard decks straight from your knowledge base.' },
    { path: '/listening', icon: 'headphones', title: 'Listening practice', description: 'Play audio tracks alongside their transcript and comprehension exercises.' },
    { path: '/grammar', icon: 'auto_stories', title: 'Grammar review', description: 'Review grammar points, prioritized by what you keep getting wrong.' },
    { path: '/review', icon: 'history', title: 'Review mistakes', description: 'See the vocabulary, grammar, and expressions you have failed most.' },
  ];

  constructor(private readonly languageService: LanguageService, private readonly bookService: BookService) {}

  ngOnInit(): void {
    forkJoin({ languages: this.languageService.list(), books: this.bookService.list() }).subscribe(({ languages, books }) => {
      this.languageCount = languages.length;
      this.bookCount = books.length;
    });
  }
}
