export interface KnowledgeFailure {
  knowledgeItemId: number;
  type: string;
  headword: string;
  summary: string | null;
  timesFailed: number;
  lastFailedAt: string | null;
}
