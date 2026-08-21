import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Topic } from '../models/topic.model';

@Injectable({ providedIn: 'root' })
export class TopicService {
  constructor(private readonly http: HttpClient) {}

  list(languageId: number): Observable<Topic[]> {
    return this.http.get<Topic[]>('/api/topics', { params: { languageId: String(languageId) } });
  }
}
