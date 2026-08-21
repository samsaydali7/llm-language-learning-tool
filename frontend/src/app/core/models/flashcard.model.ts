export interface Flashcard {
  knowledgeItemId: number;
  type: string;
  front: string;
  back: string | null;
  exampleText: string | null;
  exampleTranslation: string | null;
  bookId: number;
  page: number | null;
}
