import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { Language } from '../../core/models/language.model';
import { KnowledgeFailure } from '../../core/models/review.model';
import { Exercise } from '../../core/models/exercise.model';
import { LanguageService } from '../../core/services/language.service';
import { ReviewService } from '../../core/services/review.service';
import { ExerciseService } from '../../core/services/exercise.service';
import { ExerciseSessionComponent } from '../../shared/exercise-session/exercise-session.component';

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    ExerciseSessionComponent,
  ],
  templateUrl: './review.component.html',
  styleUrl: './review.component.scss',
})
export class ReviewComponent implements OnInit {
  languages: Language[] = [];
  languageId: number | null = null;
  failures: KnowledgeFailure[] = [];
  selected = new Set<number>();

  practiceExercises: Exercise[] | null = null;
  generating = false;

  constructor(
    private readonly languageService: LanguageService,
    private readonly reviewService: ReviewService,
    private readonly exerciseService: ExerciseService,
  ) {}

  ngOnInit(): void {
    this.languageService.list().subscribe((languages) => {
      this.languages = languages;
      if (languages.length) {
        this.languageId = languages[0].id;
        this.load();
      }
    });
  }

  load(): void {
    this.practiceExercises = null;
    this.selected = new Set();
    if (this.languageId) {
      this.reviewService.failures({ languageId: this.languageId }).subscribe((failures) => (this.failures = failures));
    }
  }

  toggle(id: number): void {
    if (this.selected.has(id)) {
      this.selected.delete(id);
    } else {
      this.selected.add(id);
    }
  }

  selectAll(): void {
    this.selected = new Set(this.failures.map((f) => f.knowledgeItemId));
  }

  practiceSelected(): void {
    const ids = this.selected.size ? Array.from(this.selected) : this.failures.map((f) => f.knowledgeItemId);
    if (ids.length === 0 || !this.languageId) {
      return;
    }
    this.generating = true;
    this.practiceExercises = null;
    this.exerciseService
      .generateOnDemand({
        scope: {
          languageId: this.languageId,
          bookId: null,
          structureNodeIds: [],
          topicIds: [],
          knowledgeTypes: [],
          knowledgeItemIds: ids,
        },
        count: Math.min(ids.length, 10),
        exerciseTypes: [],
        persist: false,
      })
      .subscribe((exercises) => {
        this.practiceExercises = exercises;
        this.generating = false;
      });
  }
}
