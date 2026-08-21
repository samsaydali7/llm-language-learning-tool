import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { Language } from '../../core/models/language.model';
import { Book } from '../../core/models/book.model';
import { Topic } from '../../core/models/topic.model';
import { StructureNode } from '../../core/models/structure.model';
import { KnowledgeItemType } from '../../core/models/knowledge.model';
import { StudyScope, emptyScope } from '../../core/models/scope.model';
import { LanguageService } from '../../core/services/language.service';
import { BookService } from '../../core/services/book.service';
import { TopicService } from '../../core/services/topic.service';
import { StructureService } from '../../core/services/structure.service';
import { StructureTreeNodeComponent } from '../structure-tree/structure-tree-node.component';

/**
 * The combinable "what to study" selector shared by exercises, flashcards, and grammar review
 * (REQUIREMENTS.md - "Choosing What to Study"). Emits a StudyScope on every change; the scope is
 * valid as soon as a language is chosen (an empty book/topic/type selection just means "no filter
 * on that dimension").
 */
@Component({
  selector: 'app-study-scope-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatCheckboxModule,
    MatIconModule,
    MatExpansionModule,
    StructureTreeNodeComponent,
  ],
  templateUrl: './study-scope-selector.component.html',
  styleUrl: './study-scope-selector.component.scss',
})
export class StudyScopeSelectorComponent implements OnInit {
  @Output() scopeChange = new EventEmitter<StudyScope>();

  languages: Language[] = [];
  books: Book[] = [];
  topics: Topic[] = [];
  structureRoots: StructureNode[] = [];

  languageId: number | null = null;
  bookId: number | null = null;
  topicIds: number[] = [];
  knowledgeTypes: KnowledgeItemType[] = [];
  selectedNodeIds = new Set<number>();

  readonly knowledgeTypeOptions: { value: KnowledgeItemType; label: string }[] = [
    { value: 'VOCABULARY', label: 'Vocabulary' },
    { value: 'GRAMMAR', label: 'Grammar' },
    { value: 'EXPRESSION', label: 'Expressions' },
  ];

  constructor(
    private readonly languageService: LanguageService,
    private readonly bookService: BookService,
    private readonly topicService: TopicService,
    private readonly structureService: StructureService,
  ) {}

  ngOnInit(): void {
    this.languageService.list().subscribe((languages) => (this.languages = languages));
  }

  onLanguageChange(): void {
    this.bookId = null;
    this.books = [];
    this.topics = [];
    this.topicIds = [];
    this.structureRoots = [];
    this.selectedNodeIds = new Set();
    if (this.languageId) {
      this.bookService.list(this.languageId).subscribe((books) => (this.books = books));
      this.topicService.list(this.languageId).subscribe((topics) => (this.topics = topics));
    }
    this.emit();
  }

  onBookChange(): void {
    this.structureRoots = [];
    this.selectedNodeIds = new Set();
    if (this.bookId) {
      this.structureService.roots(this.bookId).subscribe((roots) => (this.structureRoots = roots));
    }
    this.emit();
  }

  toggleKnowledgeType(type: KnowledgeItemType, checked: boolean): void {
    this.knowledgeTypes = checked
      ? [...this.knowledgeTypes, type]
      : this.knowledgeTypes.filter((t) => t !== type);
    this.emit();
  }

  toggleNode(nodeId: number): void {
    if (this.selectedNodeIds.has(nodeId)) {
      this.selectedNodeIds.delete(nodeId);
    } else {
      this.selectedNodeIds.add(nodeId);
    }
    this.emit();
  }

  emit(): void {
    const scope: StudyScope = {
      ...emptyScope(),
      languageId: this.languageId,
      bookId: this.bookId,
      topicIds: this.topicIds,
      knowledgeTypes: this.knowledgeTypes,
      structureNodeIds: Array.from(this.selectedNodeIds),
    };
    this.scopeChange.emit(scope);
  }
}
