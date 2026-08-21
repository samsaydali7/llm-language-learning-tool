import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, CreateBookRequest } from '../models/book.model';
import { ExtractionJob } from '../models/job.model';
import { AudioFile } from '../models/audio.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly base = '/api/books';

  constructor(private readonly http: HttpClient) {}

  list(languageId?: number): Observable<Book[]> {
    const params: Record<string, string> = {};
    if (languageId) {
      params['languageId'] = String(languageId);
    }
    return this.http.get<Book[]>(this.base, { params });
  }

  get(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.base}/${id}`);
  }

  create(request: CreateBookRequest): Observable<Book> {
    return this.http.post<Book>(this.base, request);
  }

  uploadPdf(id: number, file: File): Observable<ExtractionJob> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ExtractionJob>(`${this.base}/${id}/pdf`, formData);
  }

  uploadAudio(id: number, files: File[]): Observable<AudioFile[]> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return this.http.post<AudioFile[]>(`${this.base}/${id}/audio`, formData);
  }

  extractionJobs(id: number): Observable<ExtractionJob[]> {
    return this.http.get<ExtractionJob[]>(`${this.base}/${id}/extraction-jobs`);
  }

  /** Re-plans and re-publishes knowledge extraction against already-detected structure, without re-uploading the PDF. */
  retryExtraction(id: number): Observable<ExtractionJob> {
    return this.http.post<ExtractionJob>(`${this.base}/${id}/retry-extraction`, {});
  }
}
