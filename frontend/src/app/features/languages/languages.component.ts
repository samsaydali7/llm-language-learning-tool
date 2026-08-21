import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Language } from '../../core/models/language.model';
import { LanguageService } from '../../core/services/language.service';

@Component({
  selector: 'app-languages',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './languages.component.html',
  styleUrl: './languages.component.scss',
})
export class LanguagesComponent implements OnInit {
  languages: Language[] = [];
  code = '';
  name = '';
  saving = false;

  constructor(private readonly languageService: LanguageService, private readonly snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.languageService.list().subscribe((languages) => (this.languages = languages));
  }

  create(): void {
    if (!this.code.trim() || !this.name.trim()) {
      return;
    }
    this.saving = true;
    this.languageService.create({ code: this.code.trim(), name: this.name.trim() }).subscribe({
      next: () => {
        this.saving = false;
        this.code = '';
        this.name = '';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.snackBar.open(err.error?.message ?? 'Could not create language', 'Dismiss', { duration: 4000 });
      },
    });
  }
}
