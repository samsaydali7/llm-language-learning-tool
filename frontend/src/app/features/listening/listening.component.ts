import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { Language } from '../../core/models/language.model';
import { Book } from '../../core/models/book.model';
import { AudioFile } from '../../core/models/audio.model';
import { Exercise } from '../../core/models/exercise.model';
import { LanguageService } from '../../core/services/language.service';
import { BookService } from '../../core/services/book.service';
import { ListeningService } from '../../core/services/listening.service';
import { ExerciseSessionComponent } from '../../shared/exercise-session/exercise-session.component';

@Component({
  selector: 'app-listening',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    ExerciseSessionComponent,
  ],
  templateUrl: './listening.component.html',
  styleUrl: './listening.component.scss',
})
export class ListeningComponent implements OnInit {
  languages: Language[] = [];
  books: Book[] = [];
  tracks: AudioFile[] = [];

  languageId: number | null = null;
  bookId: number | null = null;
  selectedTrack: AudioFile | null = null;
  transcript: string | null = null;

  exercises: Exercise[] | null = null;
  generating = false;

  constructor(
    private readonly languageService: LanguageService,
    private readonly bookService: BookService,
    private readonly listeningService: ListeningService,
  ) {}

  ngOnInit(): void {
    this.languageService.list().subscribe((languages) => (this.languages = languages));
  }

  onLanguageChange(): void {
    this.bookId = null;
    this.tracks = [];
    this.selectedTrack = null;
    if (this.languageId) {
      this.bookService.list(this.languageId).subscribe((books) => (this.books = books));
    }
  }

  onBookChange(): void {
    this.selectedTrack = null;
    this.transcript = null;
    this.exercises = null;
    if (this.bookId) {
      this.listeningService.tracks(this.bookId).subscribe((tracks) => (this.tracks = tracks));
    }
  }

  selectTrack(track: AudioFile): void {
    this.selectedTrack = track;
    this.transcript = null;
    this.exercises = null;
    this.listeningService.transcript(track.id).subscribe((res) => (this.transcript = res.transcript));
  }

  streamUrl(track: AudioFile): string {
    return this.listeningService.streamUrl(track.id);
  }

  generateExercises(): void {
    if (!this.selectedTrack) {
      return;
    }
    this.generating = true;
    this.listeningService.generateExercises(this.selectedTrack.id, 5).subscribe((exercises) => {
      this.exercises = exercises;
      this.generating = false;
    });
  }
}
