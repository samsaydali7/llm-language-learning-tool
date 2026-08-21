import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KnowledgeItem } from '../models/knowledge.model';
import { StudyScope } from '../models/scope.model';

@Injectable({ providedIn: 'root' })
export class KnowledgeService {
  constructor(private readonly http: HttpClient) {}

  search(scope: StudyScope): Observable<KnowledgeItem[]> {
    return this.http.post<KnowledgeItem[]>('/api/knowledge-items/search', scope);
  }
}
