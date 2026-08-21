import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StudyScope, emptyScope } from '../../core/models/scope.model';
import { Flashcard } from '../../core/models/flashcard.model';
import { FlashcardService } from '../../core/services/flashcard.service';
import { StudyScopeSelectorComponent } from '../../shared/study-scope-selector/study-scope-selector.component';

@Component({
  selector: 'app-flashcards',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, StudyScopeSelectorComponent],
  templateUrl: './flashcards.component.html',
  styleUrl: './flashcards.component.scss',
})
export class FlashcardsComponent {
  scope: StudyScope = emptyScope();
  deck: Flashcard[] = [];
  index = 0;
  flipped = false;
  loading = false;

  constructor(private readonly flashcardService: FlashcardService) {}

  onScopeChange(scope: StudyScope): void {
    this.scope = scope;
  }

  generate(): void {
    if (!this.scope.languageId) {
      return;
    }
    this.loading = true;
    this.flashcardService.generate(this.scope).subscribe((deck) => {
      this.deck = deck;
      this.index = 0;
      this.flipped = false;
      this.loading = false;
    });
  }

  get current(): Flashcard | null {
    return this.deck[this.index] ?? null;
  }

  flip(): void {
    this.flipped = !this.flipped;
  }

  next(): void {
    if (this.index < this.deck.length - 1) {
      this.index++;
      this.flipped = false;
    }
  }

  previous(): void {
    if (this.index > 0) {
      this.index--;
      this.flipped = false;
    }
  }
}
