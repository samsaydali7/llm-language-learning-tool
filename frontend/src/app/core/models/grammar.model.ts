export interface GrammarPointSummary {
  id: number;
  title: string;
  summary: string | null;
  bookId: number;
  page: number | null;
}

export interface GrammarReviewExample {
  text: string;
  translation: string | null;
}

export interface GrammarReviewResult {
  summary: string;
  keyPoints: string[];
  commonMistakes: string[];
  reinforcementExamples: GrammarReviewExample[];
}
