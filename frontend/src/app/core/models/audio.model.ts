export interface AudioFile {
  id: number;
  bookId: number;
  originalFilename: string;
  contentType: string | null;
}

export interface AudioReference {
  id: number;
  structureNodeId: number | null;
  page: number | null;
  label: string;
  rawContext: string | null;
  audioFileId: number | null;
  audioFileName: string | null;
  matchConfidence: number | null;
}
