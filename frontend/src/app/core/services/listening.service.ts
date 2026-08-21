import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AudioFile } from '../models/audio.model';
import { Exercise } from '../models/exercise.model';

@Injectable({ providedIn: 'root' })
export class ListeningService {
  constructor(private readonly http: HttpClient) {}

  tracks(bookId: number): Observable<AudioFile[]> {
    return this.http.get<AudioFile[]>(`/api/listening/books/${bookId}/tracks`);
  }

  transcript(audioFileId: number): Observable<{ transcript: string | null }> {
    return this.http.get<{ transcript: string | null }>(`/api/listening/audio-files/${audioFileId}/transcript`);
  }

  generateExercises(audioFileId: number, count: number): Observable<Exercise[]> {
    return this.http.post<Exercise[]>(`/api/listening/audio-files/${audioFileId}/exercises?count=${count}`, {});
  }

  streamUrl(audioFileId: number): string {
    return `/api/audio-files/${audioFileId}/stream`;
  }
}
