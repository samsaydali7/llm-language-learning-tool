import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'languages',
    loadComponent: () => import('./features/languages/languages.component').then((m) => m.LanguagesComponent),
  },
  {
    path: 'books',
    loadComponent: () => import('./features/books/books.component').then((m) => m.BooksComponent),
  },
  {
    path: 'books/:id',
    loadComponent: () => import('./features/book-detail/book-detail.component').then((m) => m.BookDetailComponent),
  },
  {
    path: 'topics',
    loadComponent: () => import('./features/topics/topics.component').then((m) => m.TopicsComponent),
  },
  {
    path: 'exercises',
    loadComponent: () => import('./features/exercises/exercises.component').then((m) => m.ExercisesComponent),
  },
  {
    path: 'flashcards',
    loadComponent: () => import('./features/flashcards/flashcards.component').then((m) => m.FlashcardsComponent),
  },
  {
    path: 'listening',
    loadComponent: () => import('./features/listening/listening.component').then((m) => m.ListeningComponent),
  },
  {
    path: 'grammar',
    loadComponent: () => import('./features/grammar/grammar.component').then((m) => m.GrammarComponent),
  },
  {
    path: 'review',
    loadComponent: () => import('./features/review/review.component').then((m) => m.ReviewComponent),
  },
  { path: '**', redirectTo: '' },
];
