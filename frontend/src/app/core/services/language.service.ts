import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateLanguageRequest, Language } from '../models/language.model';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly base = '/api/languages';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Language[]> {
    return this.http.get<Language[]>(this.base);
  }

  get(id: number): Observable<Language> {
    return this.http.get<Language>(`${this.base}/${id}`);
  }

  create(request: CreateLanguageRequest): Observable<Language> {
    return this.http.post<Language>(this.base, request);
  }
}
