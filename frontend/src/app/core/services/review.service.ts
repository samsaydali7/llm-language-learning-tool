import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KnowledgeFailure } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  constructor(private readonly http: HttpClient) {}

  failures(options: { bookId?: number; languageId?: number }): Observable<KnowledgeFailure[]> {
    const params: Record<string, string> = {};
    if (options.bookId) params['bookId'] = String(options.bookId);
    if (options.languageId) params['languageId'] = String(options.languageId);
    return this.http.get<KnowledgeFailure[]>('/api/review/failures', { params });
  }
}
