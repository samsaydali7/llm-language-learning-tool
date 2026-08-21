import { KnowledgeItemType } from './knowledge.model';

/** Wire shape for a StudyScope selection - mirrors the backend's StudyScopeRequest. */
export interface StudyScope {
  languageId: number | null;
  bookId: number | null;
  structureNodeIds: number[];
  topicIds: number[];
  knowledgeTypes: KnowledgeItemType[];
  /** Optional: restrict to exactly these knowledge item ids, e.g. "practice my failures". */
  knowledgeItemIds?: number[];
}

export function emptyScope(): StudyScope {
  return { languageId: null, bookId: null, structureNodeIds: [], topicIds: [], knowledgeTypes: [] };
}
