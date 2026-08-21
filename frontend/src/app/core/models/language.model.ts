export interface Language {
  id: number;
  code: string;
  name: string;
}

export interface CreateLanguageRequest {
  code: string;
  name: string;
}
