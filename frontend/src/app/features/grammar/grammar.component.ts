import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { StudyScope, emptyScope } from '../../core/models/scope.model';
import { GrammarPointSummary, GrammarReviewResult } from '../../core/models/grammar.model';
import { GrammarService } from '../../core/services/grammar.service';
import { StudyScopeSelectorComponent } from '../../shared/study-scope-selector/study-scope-selector.component';

@Component({
  selector: 'app-grammar',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressBarModule, StudyScopeSelectorComponent],
  templateUrl: './grammar.component.html',
  styleUrl: './grammar.component.scss',
})
export class GrammarComponent {
  scope: StudyScope = emptyScope();
  points: GrammarPointSummary[] = [];
  loadingPoints = false;

  selectedPoint: GrammarPointSummary | null = null;
  review: GrammarReviewResult | null = null;
  loadingReview = false;

  constructor(private readonly grammarService: GrammarService) {}

  onScopeChange(scope: StudyScope): void {
    this.scope = scope;
  }

  loadPoints(): void {
    if (!this.scope.languageId) {
      return;
    }
    this.loadingPoints = true;
    this.selectedPoint = null;
    this.review = null;
    this.grammarService.prioritized(this.scope).subscribe((points) => {
      this.points = points;
      this.loadingPoints = false;
    });
  }

  select(point: GrammarPointSummary): void {
    this.selectedPoint = point;
    this.review = null;
    this.loadingReview = true;
    this.grammarService.review(point.id).subscribe((review) => {
      this.review = review;
      this.loadingReview = false;
    });
  }
}
