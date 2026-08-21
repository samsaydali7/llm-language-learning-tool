import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Language } from '../../core/models/language.model';
import { Book } from '../../core/models/book.model';
import { LanguageService } from '../../core/services/language.service';
import { BookService } from '../../core/services/book.service';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './books.component.html',
  styleUrl: './books.component.scss',
})
export class BooksComponent implements OnInit {
  languages: Language[] = [];
  books: Book[] = [];

  languageId: number | null = null;
  title = '';
  description = '';
  explanationLanguageCode = 'en';
  saving = false;

  constructor(
    private readonly languageService: LanguageService,
    private readonly bookService: BookService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.languageService.list().subscribe((languages) => (this.languages = languages));
    this.reload();
  }

  reload(): void {
    this.bookService.list().subscribe((books) => (this.books = books));
  }

  create(): void {
    if (!this.languageId || !this.title.trim() || !this.explanationLanguageCode.trim()) {
      return;
    }
    this.saving = true;
    this.bookService
      .create({
        languageId: this.languageId,
        title: this.title.trim(),
        description: this.description.trim() || undefined,
        explanationLanguageCode: this.explanationLanguageCode.trim(),
      })
      .subscribe({
        next: () => {
          this.saving = false;
          this.title = '';
          this.description = '';
          this.reload();
        },
        error: (err) => {
          this.saving = false;
          this.snackBar.open(err.error?.message ?? 'Could not create book', 'Dismiss', { duration: 4000 });
        },
      });
  }
}
