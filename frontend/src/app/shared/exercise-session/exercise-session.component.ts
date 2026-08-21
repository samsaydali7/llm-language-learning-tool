import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Exercise, AttemptResult } from '../../core/models/exercise.model';
import { ExerciseService } from '../../core/services/exercise.service';

/** Walks the learner through a set of exercises one at a time, grading each against the backend. */
@Component({
  selector: 'app-exercise-session',
  standalone: true,
  imports: [CommonModule, FormsModule, MatRadioModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './exercise-session.component.html',
  styleUrl: './exercise-session.component.scss',
})
export class ExerciseSessionComponent implements OnChanges {
  @Input({ required: true }) exercises: Exercise[] = [];

  index = 0;
  answer = '';
  result: AttemptResult | null = null;
  submitting = false;
  correctCount = 0;
  answeredCount = 0;

  constructor(private readonly exerciseService: ExerciseService) {}

  ngOnChanges(): void {
    this.index = 0;
    this.answer = '';
    this.result = null;
    this.correctCount = 0;
    this.answeredCount = 0;
  }

  get current(): Exercise | null {
    return this.exercises[this.index] ?? null;
  }

  get isChoiceType(): boolean {
    return this.current?.type === 'MULTIPLE_CHOICE' || this.current?.type === 'MATCHING';
  }

  submit(): void {
    if (!this.current || !this.answer.trim() || this.submitting) {
      return;
    }
    this.submitting = true;
    this.exerciseService.submitAttempt(this.current.id, this.answer.trim()).subscribe((result) => {
      this.result = result;
      this.submitting = false;
      this.answeredCount++;
      if (result.correct) {
        this.correctCount++;
      }
    });
  }

  next(): void {
    this.index++;
    this.answer = '';
    this.result = null;
  }
}
