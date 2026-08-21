import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { Language } from '../../core/models/language.model';
import { Topic } from '../../core/models/topic.model';
import { KnowledgeItem } from '../../core/models/knowledge.model';
import { LanguageService } from '../../core/services/language.service';
import { TopicService } from '../../core/services/topic.service';
import { KnowledgeService } from '../../core/services/knowledge.service';

@Component({
  selector: 'app-topics',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, MatChipsModule, MatIconModule],
  templateUrl: './topics.component.html',
  styleUrl: './topics.component.scss',
})
export class TopicsComponent implements OnInit {
  languages: Language[] = [];
  topics: Topic[] = [];
  languageId: number | null = null;
  selectedTopic: Topic | null = null;
  items: KnowledgeItem[] = [];
  loadingItems = false;

  constructor(
    private readonly languageService: LanguageService,
    private readonly topicService: TopicService,
    private readonly knowledgeService: KnowledgeService,
  ) {}

  ngOnInit(): void {
    this.languageService.list().subscribe((languages) => {
      this.languages = languages;
      if (languages.length) {
        this.languageId = languages[0].id;
        this.loadTopics();
      }
    });
  }

  loadTopics(): void {
    this.selectedTopic = null;
    this.items = [];
    if (this.languageId) {
      this.topicService.list(this.languageId).subscribe((topics) => (this.topics = topics));
    }
  }

  selectTopic(topic: Topic): void {
    this.selectedTopic = topic;
    this.loadingItems = true;
    this.knowledgeService
      .search({ languageId: this.languageId, bookId: null, structureNodeIds: [], topicIds: [topic.id], knowledgeTypes: [] })
      .subscribe((items) => {
        this.items = items;
        this.loadingItems = false;
      });
  }
}
