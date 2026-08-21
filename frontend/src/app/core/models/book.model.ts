export interface Book {
  id: number;
  languageId: number;
  languageName: string;
  title: string;
  description: string | null;
  explanationLanguageCode: string;
  hasPdf: boolean;
  pdfPageCount: number | null;
}

export interface CreateBookRequest {
  languageId: number;
  title: string;
  description?: string;
  explanationLanguageCode: string;
}
