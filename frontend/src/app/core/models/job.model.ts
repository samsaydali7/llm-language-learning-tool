export type JobStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';

export interface ExtractionJob {
  id: number;
  bookId: number;
  status: JobStatus;
  stage: 'STRUCTURE' | 'KNOWLEDGE';
  totalSections: number | null;
  completedSections: number | null;
  errorMessage: string | null;
}

export interface ExerciseGenerationJob {
  id: number;
  status: JobStatus;
  exerciseCount: number;
  errorMessage: string | null;
}
