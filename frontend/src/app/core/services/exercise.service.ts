import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AttemptResult,
  CreateExerciseJobRequest,
  Exercise,
  GenerateExercisesRequest,
} from '../models/exercise.model';
import { ExerciseGenerationJob } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class ExerciseService {
  constructor(private readonly http: HttpClient) {}

  generateOnDemand(request: GenerateExercisesRequest): Observable<Exercise[]> {
    return this.http.post<Exercise[]>('/api/exercises/generate', request);
  }

  submitAttempt(exerciseId: number, answer: string): Observable<AttemptResult> {
    return this.http.post<AttemptResult>(`/api/exercises/${exerciseId}/attempts`, { answer });
  }

  createJob(request: CreateExerciseJobRequest): Observable<ExerciseGenerationJob> {
    return this.http.post<ExerciseGenerationJob>('/api/exercise-jobs', request);
  }

  getJob(id: number): Observable<ExerciseGenerationJob> {
    return this.http.get<ExerciseGenerationJob>(`/api/exercise-jobs/${id}`);
  }

  jobExercises(id: number): Observable<Exercise[]> {
    return this.http.get<Exercise[]>(`/api/exercise-jobs/${id}/exercises`);
  }
}
