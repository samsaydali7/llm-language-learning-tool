import { StudyScope } from './scope.model';

export type ExerciseType =
  | 'MULTIPLE_CHOICE'
  | 'FILL_IN_BLANK'
  | 'TRANSLATION'
  | 'MATCHING'
  | 'SENTENCE_ORDERING'
  | 'SENTENCE_CONSTRUCTION'
  | 'GRAMMAR_TRANSFORMATION'
  | 'LISTENING_COMPREHENSION';

export const EXERCISE_TYPES: ExerciseType[] = [
  'MULTIPLE_CHOICE',
  'FILL_IN_BLANK',
  'TRANSLATION',
  'MATCHING',
  'SENTENCE_ORDERING',
  'SENTENCE_CONSTRUCTION',
  'GRAMMAR_TRANSFORMATION',
];

export interface Exercise {
  id: number;
  bookId: number | null;
  type: ExerciseType;
  prompt: string;
  options: string[];
  correctAnswer: string;
  explanation: string | null;
  audioFileId: number | null;
  sourceKnowledgeItemIds: number[];
}

export interface GenerateExercisesRequest {
  scope: StudyScope;
  count: number;
  exerciseTypes: ExerciseType[];
  persist: boolean;
}

export interface CreateExerciseJobRequest {
  scope: StudyScope;
  count: number;
  exerciseTypes: ExerciseType[];
}

export interface AttemptResult {
  correct: boolean;
  correctAnswer: string;
  explanation: string | null;
}
