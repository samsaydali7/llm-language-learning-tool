import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { StudyScope, emptyScope } from '../../core/models/scope.model';
import { EXERCISE_TYPES, Exercise, ExerciseType } from '../../core/models/exercise.model';
import { ExerciseGenerationJob } from '../../core/models/job.model';
import { ExerciseService } from '../../core/services/exercise.service';
import { StudyScopeSelectorComponent } from '../../shared/study-scope-selector/study-scope-selector.component';
import { ExerciseSessionComponent } from '../../shared/exercise-session/exercise-session.component';

@Component({
  selector: 'app-exercises',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatTabsModule,
    MatProgressBarModule,
    StudyScopeSelectorComponent,
    ExerciseSessionComponent,
  ],
  templateUrl: './exercises.component.html',
  styleUrl: './exercises.component.scss',
})
export class ExercisesComponent implements OnDestroy {
  readonly exerciseTypes = EXERCISE_TYPES;

  scope: StudyScope = emptyScope();
  count = 5;
  selectedTypes: ExerciseType[] = [];

  generating = false;
  onDemandExercises: Exercise[] | null = null;

  job: ExerciseGenerationJob | null = null;
  jobExercises: Exercise[] | null = null;
  private pollHandle: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly exerciseService: ExerciseService, private readonly snackBar: MatSnackBar) {}

  ngOnDestroy(): void {
    if (this.pollHandle) {
      clearTimeout(this.pollHandle);
    }
  }

  onScopeChange(scope: StudyScope): void {
    this.scope = scope;
  }

  toggleType(type: ExerciseType, checked: boolean): void {
    this.selectedTypes = checked ? [...this.selectedTypes, type] : this.selectedTypes.filter((t) => t !== type);
  }

  generateOnDemand(): void {
    if (!this.scope.languageId) {
      this.snackBar.open('Choose a language first', 'Dismiss', { duration: 3000 });
      return;
    }
    this.generating = true;
    this.onDemandExercises = null;
    this.exerciseService
      .generateOnDemand({ scope: this.scope, count: this.count, exerciseTypes: this.selectedTypes, persist: false })
      .subscribe({
        next: (exercises) => {
          this.generating = false;
          this.onDemandExercises = exercises;
        },
        error: (err) => {
          this.generating = false;
          this.snackBar.open(err.error?.message ?? 'Could not generate exercises', 'Dismiss', { duration: 4000 });
        },
      });
  }

  createJob(): void {
    if (!this.scope.languageId) {
      this.snackBar.open('Choose a language first', 'Dismiss', { duration: 3000 });
      return;
    }
    this.generating = true;
    this.jobExercises = null;
    this.exerciseService.createJob({ scope: this.scope, count: this.count, exerciseTypes: this.selectedTypes }).subscribe({
      next: (job) => {
        this.generating = false;
        this.job = job;
        this.pollJob();
      },
      error: (err) => {
        this.generating = false;
        this.snackBar.open(err.error?.message ?? 'Could not create exercise job', 'Dismiss', { duration: 4000 });
      },
    });
  }

  private pollJob(): void {
    if (!this.job) {
      return;
    }
    this.pollHandle = setTimeout(() => {
      this.exerciseService.getJob(this.job!.id).subscribe((job) => {
        this.job = job;
        if (job.status === 'COMPLETED') {
          this.exerciseService.jobExercises(job.id).subscribe((exercises) => (this.jobExercises = exercises));
        } else if (job.status === 'PENDING' || job.status === 'RUNNING') {
          this.pollJob();
        }
      });
    }, 2500);
  }
}
