import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GrammarPointSummary, GrammarReviewResult } from '../models/grammar.model';
import { StudyScope } from '../models/scope.model';

@Injectable({ providedIn: 'root' })
export class GrammarService {
  constructor(private readonly http: HttpClient) {}

  prioritized(scope: StudyScope): Observable<GrammarPointSummary[]> {
    return this.http.post<GrammarPointSummary[]>('/api/grammar/prioritized', scope);
  }

  review(grammarPointId: number): Observable<GrammarReviewResult> {
    return this.http.get<GrammarReviewResult>(`/api/grammar/${grammarPointId}/review`);
  }
}
