export type KnowledgeItemType = 'VOCABULARY' | 'GRAMMAR' | 'EXPRESSION';

export interface KnowledgeExample {
  text: string;
  translation: string | null;
  page: number | null;
}

export interface KnowledgeItem {
  id: number;
  type: KnowledgeItemType;
  bookId: number;
  structureNodeId: number | null;
  page: number | null;
  headword: string;
  summary: string | null;
  notes: string | null;
  detail: string | null;
  topics: string[];
  examples: KnowledgeExample[];
}
