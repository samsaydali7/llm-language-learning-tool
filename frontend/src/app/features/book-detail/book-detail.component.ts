import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Book } from '../../core/models/book.model';
import { ExtractionJob } from '../../core/models/job.model';
import { StructureNode } from '../../core/models/structure.model';
import { KnowledgeItem } from '../../core/models/knowledge.model';
import { AudioFile, AudioReference } from '../../core/models/audio.model';
import { BookService } from '../../core/services/book.service';
import { StructureService } from '../../core/services/structure.service';
import { KnowledgeService } from '../../core/services/knowledge.service';
import { AudioReferenceService } from '../../core/services/audio.service';
import { ListeningService } from '../../core/services/listening.service';
import { StructureBrowserNodeComponent } from './structure-browser-node.component';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTabsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTooltipModule,
    StructureBrowserNodeComponent,
  ],
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.scss',
})
export class BookDetailComponent implements OnInit, OnDestroy {
  book: Book | null = null;
  bookId!: number;

  structureRoots: StructureNode[] = [];
  selectedNode: StructureNode | null = null;
  selectedNodeItems: KnowledgeItem[] = [];
  loadingItems = false;

  audioFiles: AudioFile[] = [];
  audioReferences: AudioReference[] = [];

  latestJob: ExtractionJob | null = null;
  private pollHandle: ReturnType<typeof setTimeout> | null = null;

  uploadingPdf = false;
  uploadingAudio = false;
  retrying = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly structureService: StructureService,
    private readonly knowledgeService: KnowledgeService,
    private readonly audioReferenceService: AudioReferenceService,
    private readonly listeningService: ListeningService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.bookId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  ngOnDestroy(): void {
    if (this.pollHandle) {
      clearTimeout(this.pollHandle);
    }
  }

  load(): void {
    this.bookService.get(this.bookId).subscribe((book) => (this.book = book));
    this.refreshStructure();
    this.refreshAudio();
    this.bookService.extractionJobs(this.bookId).subscribe((jobs) => {
      this.latestJob = jobs[0] ?? null;
      this.maybePoll();
    });
  }

  refreshStructure(): void {
    this.structureService.roots(this.bookId).subscribe((roots) => (this.structureRoots = roots));
  }

  refreshAudio(): void {
    this.listeningService.tracks(this.bookId).subscribe((files) => (this.audioFiles = files));
    this.audioReferenceService.list(this.bookId).subscribe((refs) => (this.audioReferences = refs));
  }

  private maybePoll(): void {
    if (this.latestJob && (this.latestJob.status === 'PENDING' || this.latestJob.status === 'RUNNING')) {
      this.pollHandle = setTimeout(() => {
        this.bookService.extractionJobs(this.bookId).subscribe((jobs) => {
          this.latestJob = jobs[0] ?? null;
          if (this.latestJob?.status === 'COMPLETED') {
            this.refreshStructure();
            this.refreshAudio();
          }
          this.maybePoll();
        });
      }, 3000);
    }
  }

  onSelectNode(node: StructureNode): void {
    this.selectedNode = node;
    this.loadingItems = true;
    this.knowledgeService
      .search({ languageId: null, bookId: this.bookId, structureNodeIds: [node.id], topicIds: [], knowledgeTypes: [] })
      .subscribe((items) => {
        this.selectedNodeItems = items;
        this.loadingItems = false;
      });
  }

  onPdfSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.uploadingPdf = true;
    this.bookService.uploadPdf(this.bookId, file).subscribe({
      next: (job) => {
        this.uploadingPdf = false;
        this.latestJob = job;
        this.load();
        this.maybePoll();
      },
      error: (err) => {
        this.uploadingPdf = false;
        this.snackBar.open(err.error?.message ?? 'Could not upload PDF', 'Dismiss', { duration: 4000 });
      },
    });
  }

  onAudioSelected(event: Event): void {
    const files = Array.from((event.target as HTMLInputElement).files ?? []);
    if (files.length === 0) {
      return;
    }
    this.uploadingAudio = true;
    this.bookService.uploadAudio(this.bookId, files).subscribe({
      next: () => {
        this.uploadingAudio = false;
        this.refreshAudio();
      },
      error: (err) => {
        this.uploadingAudio = false;
        this.snackBar.open(err.error?.message ?? 'Could not upload audio', 'Dismiss', { duration: 4000 });
      },
    });
  }

  retryExtraction(): void {
    this.retrying = true;
    this.bookService.retryExtraction(this.bookId).subscribe({
      next: (job) => {
        this.retrying = false;
        this.latestJob = job;
        this.maybePoll();
      },
      error: (err) => {
        this.retrying = false;
        this.snackBar.open(err.error?.message ?? 'Could not retry extraction', 'Dismiss', { duration: 4000 });
      },
    });
  }

  linkReference(reference: AudioReference, audioFileId: number): void {
    this.audioReferenceService.link(reference.id, audioFileId).subscribe((updated) => {
      this.audioReferences = this.audioReferences.map((r) => (r.id === updated.id ? updated : r));
    });
  }
}
