import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Flashcard } from '../models/flashcard.model';
import { StudyScope } from '../models/scope.model';

@Injectable({ providedIn: 'root' })
export class FlashcardService {
  constructor(private readonly http: HttpClient) {}

  generate(scope: StudyScope): Observable<Flashcard[]> {
    return this.http.post<Flashcard[]>('/api/flashcards/generate', scope);
  }
}
