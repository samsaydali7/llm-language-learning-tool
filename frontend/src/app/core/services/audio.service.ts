import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AudioReference } from '../models/audio.model';

@Injectable({ providedIn: 'root' })
export class AudioReferenceService {
  constructor(private readonly http: HttpClient) {}

  list(bookId: number): Observable<AudioReference[]> {
    return this.http.get<AudioReference[]>(`/api/books/${bookId}/audio-references`);
  }

  link(referenceId: number, audioFileId: number): Observable<AudioReference> {
    return this.http.put<AudioReference>(`/api/audio-references/${referenceId}/link`, { audioFileId });
  }
}
